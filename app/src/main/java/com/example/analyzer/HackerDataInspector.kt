package com.example.analyzer

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Locale

enum class ThreatLevel {
    SAFE,
    WARNING,
    DANGER
}

sealed class ParsedPayload {
    data class Url(
        val url: String,
        val scheme: String,
        val host: String,
        val path: String,
        val queryParams: Map<String, String>,
        val isHttps: Boolean,
        val isIpAddress: Boolean,
        val isSuspiciousTld: Boolean
    ) : ParsedPayload()

    data class WiFi(
        val ssid: String,
        val password: String,
        val authType: String, // WPA, WEP, NONE
        val isHidden: Boolean
    ) : ParsedPayload()

    data class Upi(
        val vpa: String,
        val payeeName: String,
        val amount: String?,
        val currency: String,
        val note: String?,
        val transactionRef: String?
    ) : ParsedPayload()

    data class VCard(
        val formattedName: String,
        val phoneNumbers: List<String>,
        val emails: List<String>,
        val organization: String?,
        val title: String?,
        val url: String?
    ) : ParsedPayload()

    data class Geo(
        val latitude: Double,
        val longitude: Double,
        val query: String?
    ) : ParsedPayload()

    data class Email(
        val address: String,
        val subject: String?,
        val body: String?
    ) : ParsedPayload()

    data class Phone(
        val number: String
    ) : ParsedPayload()

    data class Sms(
        val number: String,
        val message: String?
    ) : ParsedPayload()

    data class Text(
        val content: String,
        val isBase64: Boolean = false,
        val decodedBase64: String? = null
    ) : ParsedPayload()
}

data class InspectionReport(
    val raw: String,
    val format: String,
    val byteCount: Int,
    val charSet: String,
    val hexDump: String,
    val mimeType: String,
    val threatLevel: ThreatLevel,
    val securityReasons: List<String>,
    val payload: ParsedPayload
)

object HackerDataInspector {

    private val SUSPICIOUS_TLDS = setOf(
        "xyz", "top", "work", "loan", "click", "racing", "date", "men", "party", "gq", "cf", "ml", "tk", "ga"
    )

    fun inspect(raw: String, format: String = "QR_CODE"): InspectionReport {
        val trimmed = raw.trim()
        val bytes = raw.toByteArray(StandardCharsets.UTF_8)
        val hexDump = generateHexDump(bytes)
        val charSet = detectCharset(bytes)

        val parsed = parsePayload(trimmed)
        val securityResult = analyzeSecurity(trimmed, parsed)
        val mimeType = detectMimeType(trimmed, parsed)

        return InspectionReport(
            raw = raw,
            format = format,
            byteCount = bytes.size,
            charSet = charSet,
            hexDump = hexDump,
            mimeType = mimeType,
            threatLevel = securityResult.first,
            securityReasons = securityResult.second,
            payload = parsed
        )
    }

    private fun generateHexDump(bytes: ByteArray): String {
        if (bytes.isEmpty()) return "00000000: (empty payload)"
        val sb = StringBuilder()
        val len = bytes.size
        for (i in 0 until len step 16) {
            sb.append(String.format(Locale.US, "%08X: ", i))
            val chunkLimit = (i + 16).coerceAtMost(len)

            // Hex pairs
            for (j in i until chunkLimit) {
                sb.append(String.format(Locale.US, "%02x", bytes[j]))
                if ((j - i) % 2 == 1) sb.append(" ")
            }
            // Spacing for incomplete lines
            if (chunkLimit - i < 16) {
                val missing = 16 - (chunkLimit - i)
                val spaces = missing * 2 + (missing / 2)
                sb.append(" ".repeat(spaces))
            }
            sb.append(" |")
            // ASCII representation
            for (j in i until chunkLimit) {
                val b = bytes[j].toInt()
                if (b in 32..126) {
                    sb.append(b.toChar())
                } else {
                    sb.append('.')
                }
            }
            sb.append("|\n")
        }
        return sb.toString().trimEnd()
    }

    private fun detectCharset(bytes: ByteArray): String {
        var isAscii = true
        for (b in bytes) {
            if (b.toInt() and 0x80 != 0) {
                isAscii = false
                break
            }
        }
        return if (isAscii) "US-ASCII (7-bit clean)" else "UTF-8 (Multibyte Unicode)"
    }

    private fun detectMimeType(raw: String, payload: ParsedPayload): String {
        return when (payload) {
            is ParsedPayload.Url -> "text/uri-list"
            is ParsedPayload.WiFi -> "application/vnd.wifi.p2p"
            is ParsedPayload.Upi -> "application/x-upi-payment"
            is ParsedPayload.VCard -> "text/vcard"
            is ParsedPayload.Geo -> "application/vnd.google-earth.kml+xml"
            is ParsedPayload.Email -> "message/rfc822"
            is ParsedPayload.Phone -> "text/x-phone"
            is ParsedPayload.Sms -> "text/x-sms"
            is ParsedPayload.Text -> {
                if (raw.startsWith("{") && raw.endsWith("}")) "application/json"
                else if (raw.startsWith("<") && raw.endsWith(">")) "application/xml"
                else "text/plain"
            }
        }
    }

    private fun extractQueryParams(queryString: String?): Map<String, String> {
        if (queryString.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        val pairs = queryString.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = try { URLDecoder.decode(pair.substring(0, idx), "UTF-8") } catch (_: Exception) { pair.substring(0, idx) }
                val value = try { URLDecoder.decode(pair.substring(idx + 1), "UTF-8") } catch (_: Exception) { pair.substring(idx + 1) }
                map[key] = value
            }
        }
        return map
    }

    private fun parsePayload(raw: String): ParsedPayload {
        val lower = raw.lowercase(Locale.ROOT)

        // 1. WiFi parsing (WIFI:S:ssid;T:WPA;P:password;H:false;;)
        if (raw.startsWith("WIFI:", ignoreCase = true)) {
            var ssid = ""
            var password = ""
            var auth = "WPA"
            var hidden = false
            val tokens = raw.removePrefix("WIFI:").removePrefix("wifi:").split(";")
            for (token in tokens) {
                val trimmed = token.trim()
                when {
                    trimmed.startsWith("S:", ignoreCase = true) -> ssid = trimmed.substring(2)
                    trimmed.startsWith("P:", ignoreCase = true) -> password = trimmed.substring(2)
                    trimmed.startsWith("T:", ignoreCase = true) -> auth = trimmed.substring(2)
                    trimmed.startsWith("H:", ignoreCase = true) -> hidden = trimmed.substring(2).toBoolean()
                }
            }
            return ParsedPayload.WiFi(ssid = ssid, password = password, authType = auth, isHidden = hidden)
        }

        // 2. UPI Payment (upi://pay?pa=...&pn=...)
        if (lower.startsWith("upi://pay")) {
            val queryPart = if (raw.contains("?")) raw.substringAfter("?") else ""
            val params = extractQueryParams(queryPart)
            val pa = params["pa"] ?: ""
            val pn = params["pn"] ?: ""
            val am = params["am"]
            val cu = params["cu"] ?: "INR"
            val tn = params["tn"]
            val tr = params["tr"]
            return ParsedPayload.Upi(
                vpa = pa,
                payeeName = pn.ifBlank { "Merchant" },
                amount = am,
                currency = cu,
                note = tn,
                transactionRef = tr
            )
        }

        // 3. vCard parsing (BEGIN:VCARD ... END:VCARD)
        if (raw.contains("BEGIN:VCARD", ignoreCase = true)) {
            val lines = raw.lines()
            var fn = ""
            val phones = mutableListOf<String>()
            val emails = mutableListOf<String>()
            var org: String? = null
            var title: String? = null
            var url: String? = null

            for (line in lines) {
                val clean = line.trim()
                when {
                    clean.startsWith("FN:", ignoreCase = true) -> fn = clean.substring(3)
                    clean.startsWith("N:", ignoreCase = true) && fn.isBlank() -> fn = clean.substring(2).replace(";", " ").trim()
                    clean.startsWith("TEL", ignoreCase = true) -> {
                        val colon = clean.indexOf(':')
                        if (colon != -1) phones.add(clean.substring(colon + 1))
                    }
                    clean.startsWith("EMAIL", ignoreCase = true) -> {
                        val colon = clean.indexOf(':')
                        if (colon != -1) emails.add(clean.substring(colon + 1))
                    }
                    clean.startsWith("ORG:", ignoreCase = true) -> org = clean.substring(4)
                    clean.startsWith("TITLE:", ignoreCase = true) -> title = clean.substring(6)
                    clean.startsWith("URL:", ignoreCase = true) -> url = clean.substring(4)
                }
            }
            return ParsedPayload.VCard(
                formattedName = fn.ifBlank { "Contact" },
                phoneNumbers = phones,
                emails = emails,
                organization = org,
                title = title,
                url = url
            )
        }

        // 4. Geo coordinates (geo:37.7749,-122.4194 or geo:0,0?q=...)
        if (lower.startsWith("geo:")) {
            try {
                val body = raw.substring(4)
                val parts = body.split("?")
                val coords = parts[0].split(",")
                val lat = coords.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val lng = coords.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                val queryParams = if (parts.size > 1) extractQueryParams(parts[1]) else emptyMap()
                val q = queryParams["q"]
                return ParsedPayload.Geo(latitude = lat, longitude = lng, query = q)
            } catch (_: Exception) {}
        }

        // 5. Telephone & SMS
        if (lower.startsWith("tel:")) {
            return ParsedPayload.Phone(raw.substring(4).trim())
        }
        if (lower.startsWith("smsto:") || lower.startsWith("sms:")) {
            val prefix = if (lower.startsWith("smsto:")) "smsto:" else "sms:"
            val body = raw.substring(prefix.length)
            val parts = body.split(":", limit = 2)
            return ParsedPayload.Sms(number = parts[0], message = parts.getOrNull(1))
        }

        // 6. Email (mailto: or MATMSG:)
        if (lower.startsWith("mailto:")) {
            val schemeSpecific = raw.substring(7)
            val address = schemeSpecific.substringBefore("?")
            val query = if (schemeSpecific.contains("?")) schemeSpecific.substringAfter("?") else ""
            val params = extractQueryParams(query)
            return ParsedPayload.Email(
                address = address,
                subject = params["subject"],
                body = params["body"]
            )
        }
        if (lower.startsWith("matmsg:")) {
            var to = ""
            var sub: String? = null
            var body: String? = null
            val tokens = raw.removePrefix("MATMSG:").removePrefix("matmsg:").split(";")
            for (t in tokens) {
                if (t.startsWith("TO:", ignoreCase = true)) to = t.substring(3)
                if (t.startsWith("SUB:", ignoreCase = true)) sub = t.substring(4)
                if (t.startsWith("BODY:", ignoreCase = true)) body = t.substring(5)
            }
            return ParsedPayload.Email(address = to, subject = sub, body = body)
        }

        // 7. URL detection (http://, https://, or domain pattern)
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("ftp://")) {
            return parseUrl(raw)
        }

        // 8. Plain text & Base64 test
        val isBase64 = isBase64String(raw)
        var decodedString: String? = null
        if (isBase64 && raw.length in 8..4096) {
            try {
                val decoded = Base64.getDecoder().decode(raw.trim())
                decodedString = String(decoded, StandardCharsets.UTF_8)
            } catch (_: Exception) {}
        }

        return ParsedPayload.Text(
            content = raw,
            isBase64 = isBase64,
            decodedBase64 = decodedString
        )
    }

    private fun parseUrl(raw: String): ParsedPayload.Url {
        val scheme = raw.substringBefore("://", "http")
        val afterScheme = raw.substringAfter("://", raw)
        val hostAndRest = afterScheme.substringBefore("?")
        val host = hostAndRest.substringBefore("/")
        val path = if (hostAndRest.contains("/")) "/" + hostAndRest.substringAfter("/") else "/"
        val queryPart = if (raw.contains("?")) raw.substringAfter("?") else ""
        val params = extractQueryParams(queryPart)

        val isHttps = scheme.equals("https", ignoreCase = true)
        val isIp = host.matches(Regex("""^(\d{1,3}\.){3}\d{1,3}$"""))

        val tld = host.substringAfterLast(".", "")
        val isSuspiciousTld = SUSPICIOUS_TLDS.contains(tld.lowercase(Locale.ROOT))

        return ParsedPayload.Url(
            url = raw,
            scheme = scheme,
            host = host,
            path = path,
            queryParams = params,
            isHttps = isHttps,
            isIpAddress = isIp,
            isSuspiciousTld = isSuspiciousTld
        )
    }

    private fun analyzeSecurity(raw: String, payload: ParsedPayload): Pair<ThreatLevel, List<String>> {
        val reasons = mutableListOf<String>()

        when (payload) {
            is ParsedPayload.Url -> {
                if (!payload.isHttps) {
                    reasons.add("Unencrypted protocol (HTTP). Sensitive credentials can be intercepted.")
                }
                if (payload.isIpAddress) {
                    reasons.add("Raw IP address host detected. Often used to circumvent domain reputation filters.")
                }
                if (payload.isSuspiciousTld) {
                    reasons.add("High-risk top level domain (.${payload.host.substringAfterLast('.')}) frequently linked to spam or phishing.")
                }
                if (payload.queryParams.any { it.key.contains("token", true) || it.key.contains("auth", true) || it.key.contains("password", true) }) {
                    reasons.add("Sensitive authentication token found in URL query parameters.")
                }
                if (raw.length > 250) {
                    reasons.add("Abnormally long URL parameter payload.")
                }

                return when {
                    payload.isIpAddress || (payload.isSuspiciousTld && !payload.isHttps) -> ThreatLevel.DANGER to reasons
                    !payload.isHttps || payload.isSuspiciousTld -> ThreatLevel.WARNING to reasons
                    else -> {
                        reasons.add("TLS encrypted (HTTPS). No anomalous domain indicators flagged.")
                        ThreatLevel.SAFE to reasons
                    }
                }
            }
            is ParsedPayload.WiFi -> {
                if (payload.authType.equals("NONE", ignoreCase = true) || payload.authType.equals("OPEN", ignoreCase = true)) {
                    reasons.add("Unsecured open WiFi network. No encryption protocol.")
                    return ThreatLevel.WARNING to reasons
                }
                reasons.add("WPA/WPA2 encrypted network credentials.")
                return ThreatLevel.SAFE to reasons
            }
            is ParsedPayload.Upi -> {
                reasons.add("UPI Digital Payment intent.")
                if (payload.amount != null) {
                    reasons.add("Specified transaction value: ₹${payload.amount}")
                }
                return ThreatLevel.SAFE to reasons
            }
            else -> {
                reasons.add("Standard data payload verified.")
                return ThreatLevel.SAFE to reasons
            }
        }
    }

    private fun isBase64String(str: String): Boolean {
        val trimmed = str.trim()
        if (trimmed.length < 8 || trimmed.length % 4 != 0) return false
        return trimmed.matches(Regex("""^[A-Za-z0-9+/]+={0,2}$"""))
    }
}
