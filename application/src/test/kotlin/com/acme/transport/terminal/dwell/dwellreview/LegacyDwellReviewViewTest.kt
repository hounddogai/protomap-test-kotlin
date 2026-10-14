package com.acme.transport.terminal.dwell.dwellreview

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDwellReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDwellReview("code-823", "label-647")
        val view = toLegacyDwellReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDwellReviewViews(mutableListOf<LegacyDwellReview>(item, item)).size, "views")
    }
}
