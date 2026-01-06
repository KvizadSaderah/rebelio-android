package com.kaidendev.rebelioclientandroid.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaidendev.rebelioclientandroid.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "🚫📱",
        title = "No Phone Numbers",
        description = "Rebelio doesn't need your phone number or email.\nYour identity is a cryptographic key."
    ),
    OnboardingPage(
        emoji = "🔐",
        title = "Your Keys, Your Control",
        description = "Keys are stored ONLY on this device.\nNo cloud. No recovery.\n\nIf you delete the app — your account is gone forever."
    ),
    OnboardingPage(
        emoji = "📲",
        title = "Exchange Contacts",
        description = "To start chatting, exchange QR codes with your contact.\n\nNo server-side address books."
    ),
    OnboardingPage(
        emoji = "✅",
        title = "Ready!",
        description = "You're ready for private messaging.\n\nCreate your identity and get started."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg)
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < onboardingPages.size - 1) {
                    TextButton(onClick = onComplete) {
                        Text("Skip", color = TextMuted)
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(onboardingPages[page])
            }

            // Page Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.lg),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val scale = animateFloatAsState(if (isSelected) 1.2f else 1f, label = "dot_scale")
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .scale(scale.value)
                            .clip(CircleShape)
                            .background(if (isSelected) MatrixGreen else TextMuted.copy(alpha = 0.5f))
                    )
                }
            }

            // Navigation Button
            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatrixGreen,
                    contentColor = DeepBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage < onboardingPages.size - 1) "NEXT" else "GET STARTED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji
        Text(
            text = page.emoji,
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = Spacing.xl)
        )

        // Title
        Text(
            text = page.title,
            style = RebelioTypography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Description
        Text(
            text = page.description,
            style = RebelioTypography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
