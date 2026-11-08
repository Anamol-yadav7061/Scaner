package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analyzer.ThreatLevel
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.ThreatDanger
import com.example.ui.theme.ThreatDangerBg
import com.example.ui.theme.ThreatSafe
import com.example.ui.theme.ThreatSafeBg
import com.example.ui.theme.ThreatWarning
import com.example.ui.theme.ThreatWarningBg

@Composable
fun SecurityBadge(
    threatLevel: ThreatLevel,
    reasons: List<String>,
    modifier: Modifier = Modifier
) {
    val (badgeColor, bgColor, icon, title) = when (threatLevel) {
        ThreatLevel.SAFE -> Quadruple(
            ThreatSafe,
            ThreatSafeBg,
            Icons.Default.GppGood,
            "SECURE & VERIFIED"
        )
        ThreatLevel.WARNING -> Quadruple(
            ThreatWarning,
            ThreatWarningBg,
            Icons.Default.GppMaybe,
            "POTENTIAL RISKS DETECTED"
        )
        ThreatLevel.DANGER -> Quadruple(
            ThreatDanger,
            ThreatDangerBg,
            Icons.Default.Warning,
            "CRITICAL THREAT / PHISHING RISK"
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = badgeColor,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        if (reasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            reasons.forEach { reason ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = badgeColor,
                        fontFamily = MonospaceFont,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reason,
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
