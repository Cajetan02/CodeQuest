package com.codequest.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    val annotatedString = parseSimpleMarkdown(
        text = text,
        codeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        codeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style
    )
}

@Composable
private fun parseSimpleMarkdown(
    text: String,
    codeBackgroundColor: androidx.compose.ui.graphics.Color,
    codeTextColor: androidx.compose.ui.graphics.Color
): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        while (currentIndex < text.length) {
            // Check for bold (**text**)
            if (currentIndex + 1 < text.length && text[currentIndex] == '*' && text[currentIndex + 1] == '*') {
                val endIndex = text.indexOf("**", currentIndex + 2)
                if (endIndex != -1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(currentIndex + 2, endIndex))
                    }
                    currentIndex = endIndex + 2
                    continue
                }
            }
            
            // Check for italic (*text* or _text_)
            if (text[currentIndex] == '*' || text[currentIndex] == '_') {
                val char = text[currentIndex]
                val endIndex = text.indexOf(char, currentIndex + 1)
                // Ensure there's no space immediately following the starting marker, simple heuristic
                if (endIndex != -1 && endIndex > currentIndex + 1 && text[currentIndex + 1] != ' ') {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(currentIndex + 1, endIndex))
                    }
                    currentIndex = endIndex + 1
                    continue
                }
            }

            // Check for inline code (`code`)
            if (text[currentIndex] == '`') {
                val endIndex = text.indexOf('`', currentIndex + 1)
                if (endIndex != -1) {
                    withStyle(style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBackgroundColor,
                        color = codeTextColor,
                        fontSize = 14.sp
                    )) {
                        // Adding zero-width spaces or padding isn't easily possible in annotated string without background clipping natively.
                        // For inline code, just the style applies.
                        append(text.substring(currentIndex + 1, endIndex))
                    }
                    currentIndex = endIndex + 1
                    continue
                }
            }
            
            // If none matched, append the character as normal
            append(text[currentIndex])
            currentIndex++
        }
    }
}
