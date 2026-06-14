package com.cartharsis.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.Currency
import com.cartharsis.ui.theme.CartharsisTheme

/** The compact field that replaced the chip wall — the closed (resting) state. */
@PreviewTest
@Preview(name = "Currency field", showBackground = true, widthDp = 360)
@Composable
internal fun CurrencyFieldPreview() {
    CartharsisTheme {
        Surface {
            // CurrencySelector renders the field; the sheet stays closed here.
            Surface(Modifier.padding(16.dp)) {
                CurrencySelector(selected = Currency.KRW, onSelect = {})
            }
        }
    }
}

/** The bottom-sheet body: the full currency list (sheets don't render in previews). */
@PreviewTest
@Preview(name = "Currency picker list", showBackground = true, widthDp = 360)
@Composable
internal fun CurrencyPickerContentPreview() {
    CartharsisTheme {
        Surface {
            CurrencyPickerContent(selected = Currency.EUR, onSelect = {})
        }
    }
}

@PreviewTest
@Preview(
    name = "Currency picker list, dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun CurrencyPickerContentDarkPreview() {
    CartharsisTheme {
        Surface {
            CurrencyPickerContent(selected = Currency.KRW, onSelect = {})
        }
    }
}
