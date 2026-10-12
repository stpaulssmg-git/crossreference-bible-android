package com.stpauls.mycrossreferencebible.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stpauls.mycrossreferencebible.ui.theme.MyCrossReferenceBibleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderUiMockup(
    showBottomSheet: Boolean = true
) {
    MyCrossReferenceBibleTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBF9F5)) // Warm paper background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Genesis 1",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1C1B1F)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF5F0E6)
                    )
                )

                Divider(color = Color(0xFFE0D8C8))

                // Reading Content Area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Verse 1
                    Row(modifier = Modifier.padding(bottom = 12.dp)) {
                        Text(
                            text = "1 ",
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val v1Text = buildAnnotatedString {
                            append("In the ")
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("beginning")
                            }
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("God")
                            }
                            append(" created the heaven and the earth.")
                        }
                        Text(
                            text = v1Text,
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            color = Color(0xFF2C2C2C)
                        )
                    }

                    // Verse 2
                    Row(modifier = Modifier.padding(bottom = 12.dp)) {
                        Text(
                            text = "2 ",
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val v2Text = buildAnnotatedString {
                            append("And the earth was ")
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("without")
                            }
                            append(" form, and void; and darkness [was] upon the face of the deep. And the ")
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Spirit")
                            }
                            append(" of God moved upon the face of the waters.")
                        }
                        Text(
                            text = v2Text,
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            color = Color(0xFF2C2C2C)
                        )
                    }

                    // Verse 3
                    Row(modifier = Modifier.padding(bottom = 12.dp)) {
                        Text(
                            text = "3 ",
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val v3Text = buildAnnotatedString {
                            append("And ")
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("God")
                            }
                            append(" said, ")
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Let")
                            }
                            append(" there be light: and there was light.")
                        }
                        Text(
                            text = v3Text,
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            color = Color(0xFF2C2C2C)
                        )
                    }
                }
            }

            // Bottom Sheet overlay if active
            if (showBottomSheet) {
                // Dimmed background overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                // Bottom Sheet Card
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color.LightGray, RoundedCornerShape(2.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Header
                        Text(
                            text = "Cross References for \"beginning\"",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = "Genesis 1:1",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Reference items list
                        val referencesList = listOf(
                            "Prov 8:22-24" to "The LORD possessed me in the beginning of his way, before his works of old.",
                            "John 1:1-3" to "In the beginning was the Word, and the Word was with God, and the Word was God.",
                            "Heb 1:10" to "And, Thou, Lord, in the beginning hast laid the foundation of the earth...",
                            "1 John 1:1" to "That which was from the beginning, which we have heard, which we have seen...",
                            "Mark 13:19" to "For in those days shall be affliction, such as was not from the beginning..."
                        )

                        referencesList.forEach { (ref, preview) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = ref,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFB71C1C)
                                )
                                Text(
                                    text = preview,
                                    fontSize = 14.sp,
                                    color = Color(0xFF444444),
                                    maxLines = 2
                                )
                            }
                            Divider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 600)
@Composable
fun BibleReaderOnlyPreview() {
    BibleReaderUiMockup(showBottomSheet = false)
}

@Preview(showBackground = true, widthDp = 380, heightDp = 700)
@Composable
fun BibleReaderUiPreview() {
    BibleReaderUiMockup(showBottomSheet = true)
}
