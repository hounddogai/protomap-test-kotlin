package com.acme.transport.terminal.dwell.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellReview("code-977", "title-668")
        val view = toDwellReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellReviewViews(mutableListOf<DwellReview>(item, item)).size, "views")
    }
}
