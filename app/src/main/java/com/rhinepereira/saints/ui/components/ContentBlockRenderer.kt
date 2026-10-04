package com.rhinepereira.saints.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rhinepereira.saints.data.model.ContentBlock
import com.rhinepereira.saints.ui.theme.ReadingText

@Composable
fun ContentBlockRenderer(block: ContentBlock) {
    val value = block.value?.takeIf { it.isNotBlank() } ?: return
    when (block.type) {
        "heading" -> Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        "image" -> AsyncImage(
            model = value,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
        )
        else -> Text(value, style = ReadingText, modifier = Modifier.fillMaxWidth())
    }
}
