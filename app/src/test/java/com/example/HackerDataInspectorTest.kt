package com.example

import com.example.analyzer.HackerDataInspector
import com.example.analyzer.ParsedPayload
import com.example.analyzer.ThreatLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HackerDataInspectorTest {

    @Test
    fun testUrlInspectionAndHttpsSafety() {
        val safeReport = HackerDataInspector.inspect("https://example.com/login?param=1")
        assertEquals(ThreatLevel.SAFE, safeReport.threatLevel)
        assertTrue(safeReport.payload is ParsedPayload.Url)
        val urlPayload = safeReport.payload as ParsedPayload.Url
        assertTrue(urlPayload.isHttps)
        assertEquals("example.com", urlPayload.host)

        val unencryptedReport = HackerDataInspector.inspect("http://phishing-site.xyz/login")
        assertEquals(ThreatLevel.DANGER, unencryptedReport.threatLevel)
    }

    @Test
    fun testWifiParsing() {
        val wifiReport = HackerDataInspector.inspect("WIFI:S:CyberLab;T:WPA;P:MatrixKey2026;H:false;;")
        assertTrue(wifiReport.payload is ParsedPayload.WiFi)
        val wifi = wifiReport.payload as ParsedPayload.WiFi
        assertEquals("CyberLab", wifi.ssid)
        assertEquals("MatrixKey2026", wifi.password)
        assertEquals("WPA", wifi.authType)
    }

    @Test
    fun testUpiParsing() {
        val upiReport = HackerDataInspector.inspect("upi://pay?pa=cyber@okhdfcbank&pn=CyberMerchant&am=250.00&cu=INR")
        assertTrue(upiReport.payload is ParsedPayload.Upi)
        val upi = upiReport.payload as ParsedPayload.Upi
        assertEquals("cyber@okhdfcbank", upi.vpa)
        assertEquals("CyberMerchant", upi.payeeName)
        assertEquals("250.00", upi.amount)
    }

    @Test
    fun testHexDumpGeneration() {
        val report = HackerDataInspector.inspect("HELLO WORLD")
        assertNotNull(report.hexDump)
        assertTrue(report.hexDump.contains("00000000:"))
        assertTrue(report.hexDump.contains("HELLO WORLD"))
    }
}
