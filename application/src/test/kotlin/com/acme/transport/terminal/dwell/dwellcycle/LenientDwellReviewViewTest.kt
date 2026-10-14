package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDwellReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDwellReview("code-824", "label-889")
        val view = toLenientDwellReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDwellReviewViews(mutableListOf<LenientDwellReview>(item, item)).size, "views")
    }
}
