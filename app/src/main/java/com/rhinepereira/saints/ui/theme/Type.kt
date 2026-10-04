package com.rhinepereira.saints.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rhinepereira.saints.R

// Yatra One: signpainted display (Latin + Devanagari). Mukta: UI. Martel: reading text.
val Signpaint = FontFamily(Font(R.font.yatra_one))
val Mukta = FontFamily(
    Font(R.font.mukta_regular, FontWeight.Normal),
    Font(R.font.mukta_medium, FontWeight.Medium),
    Font(R.font.mukta_semibold, FontWeight.SemiBold),
    Font(R.font.mukta_bold, FontWeight.Bold)
)
val Martel = FontFamily(
    Font(R.font.martel_regular, FontWeight.Normal),
    Font(R.font.martel_bold, FontWeight.Bold)
)

private fun sign(size: Int, line: Int) =
    TextStyle(fontFamily = Signpaint, fontSize = size.sp, lineHeight = line.sp, letterSpacing = (-0.01).em)

private fun ui(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: Double = 0.0) =
    TextStyle(fontFamily = Mukta, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = tracking.em)

val Typography = Typography(
    displayLarge = sign(96, 96),
    displayMedium = sign(56, 60),
    displaySmall = sign(40, 46),
    headlineLarge = sign(32, 40),
    headlineMedium = sign(26, 34),
    headlineSmall = ui(22, 30, FontWeight.Bold),
    titleLarge = ui(20, 28, FontWeight.Bold),
    titleMedium = ui(17, 24, FontWeight.SemiBold),
    titleSmall = ui(15, 20, FontWeight.SemiBold),
    bodyLarge = ui(17, 27),
    bodyMedium = ui(15, 23),
    bodySmall = ui(13, 19),
    labelLarge = ui(15, 20, FontWeight.SemiBold, 0.01),
    labelMedium = ui(13, 18, FontWeight.SemiBold, 0.02),
    labelSmall = ui(12, 16, FontWeight.SemiBold, 0.03)
)

/** Long-form reading text (Scripture, prayers, Catechism). */
val ReadingText = TextStyle(fontFamily = Martel, fontSize = 18.sp, lineHeight = 32.sp)
