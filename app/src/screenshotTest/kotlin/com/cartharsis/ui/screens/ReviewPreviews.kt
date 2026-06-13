package com.cartharsis.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cartharsis.data.FakeCatalog
import com.cartharsis.data.Review
import com.cartharsis.data.UserReview
import com.cartharsis.ui.theme.CartharsisTheme

// Drive the cards off real pool reviews so the previews track the shipped
// voice, not invented fixtures. One of each star band the rating bars promise.
private val pooledReviews = FakeCatalog.products.flatMap { it.reviews }
private val fiveStar = pooledReviews.first { it.rating == 5 }
private val fourStar = pooledReviews.first { it.rating == 4 }
private val lowStar = pooledReviews.first { it.rating <= 2 }
private val longestReview = pooledReviews.maxByOrNull { it.text.length }!!

private val summaryProduct = FakeCatalog.products.first { it.reviewCount > 5000 }

@Composable
private fun reviewCard(review: Review, ageLabel: String, helpful: Int) {
    ReviewCard(review.author, review.rating, review.text, ageLabel, helpful)
}

/**
 * ReviewCard across the star bands a real product shows: a glowing five, a
 * qualified four, a one-star poet, and the longest body in the pool (the
 * multi-line wrap). The low-star case is the one to watch, its rating glyphs
 * must still read as "1 of 5", not a lonely star.
 */
@PreviewTest
@Preview(name = "ReviewCard, star bands", showBackground = true, widthDp = 380)
@Composable
internal fun ReviewCardBandsPreview() {
    CartharsisTheme {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            reviewCard(fiveStar, "3d ago", 128)
            reviewCard(fourStar, "2w ago", 47)
            reviewCard(lowStar, "5d ago", 9)
            reviewCard(longestReview, "1w ago", 212)
        }
    }
}

/** The same cards on night, where the avatar tints and the gold stars have to
 * hold against a dark surface. */
@PreviewTest
@Preview(
    name = "ReviewCard, dark",
    showBackground = true,
    widthDp = 380,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun ReviewCardDarkPreview() {
    CartharsisTheme {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            reviewCard(fiveStar, "3d ago", 128)
            reviewCard(lowStar, "5d ago", 9)
        }
    }
}

/** The rating breakdown that heads the reviews section: the big average, the
 * star row, and the five distribution bars. */
@PreviewTest
@Preview(name = "RatingSummary", showBackground = true, widthDp = 380)
@Composable
internal fun RatingSummaryPreview() {
    CartharsisTheme {
        Column(Modifier.padding(12.dp)) {
            RatingSummary(summaryProduct)
        }
    }
}

/** The user's own pinned review (above the regulars) and the write-a-review
 * editor in its empty and rated states. */
@PreviewTest
@Preview(name = "Own review + editor", showBackground = true, widthDp = 380)
@Composable
internal fun OwnReviewAndEditorPreview() {
    CartharsisTheme {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OwnReviewCard(
                review = UserReview(
                    productId = 0,
                    rating = 5,
                    text = "Best nothing I never bought. The anticipation alone paid for itself.",
                    createdAtMillis = 1_717_200_000_000L,
                ),
                onEdit = {},
                onDelete = {},
            )
            ReviewEditor(initialRating = 0, initialText = "", onCancel = {}, onPost = { _, _ -> })
            ReviewEditor(
                initialRating = 4,
                initialText = "Wanted to want it more.",
                onCancel = {},
                onPost = { _, _ -> },
            )
        }
    }
}
