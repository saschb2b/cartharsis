package com.cartharsis.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.encodeBinderCard
import com.cartharsis.ui.theme.CartharsisTheme

/** The Moppling display cabinet, mid-collection: found figures standing,
 * blanks waiting. */
@PreviewTest
@Preview(name = "Moppling shelf, partly found", showBackground = true, heightDp = 980)
@Composable
internal fun MopplingShelfPreview() {
    val found = setOf(
        encodeBinderCard("bog", "Slowmop"),
        encodeBinderCard("bog", "Puddlewink"),
        encodeBinderCard("bog", "Snapjaw Jr."),
        encodeBinderCard("cloud", "Moonmop"),
        encodeBinderCard("cloud", "Prismling"),
        encodeBinderCard("charm", "Wobbles"),
        encodeBinderCard("gremlin", "Mondayman"),
        encodeBinderCard("gremlin", "Overtime"),
    )
    CartharsisTheme {
        MopplingShelf(shelf = found, modifier = Modifier.padding(12.dp))
    }
}
