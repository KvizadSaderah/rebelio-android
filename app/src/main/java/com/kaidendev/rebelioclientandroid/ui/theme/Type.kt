package com.kaidendev.rebelioclientandroid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kaidendev.rebelioclientandroid.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

val RebelioTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 11.sp,
        color = TextMuted,
        fontWeight = FontWeight.Medium
    )
)
