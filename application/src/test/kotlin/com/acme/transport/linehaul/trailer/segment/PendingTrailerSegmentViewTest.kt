package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTrailerSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTrailerSegment("code-670", "label-905")
        val view = toPendingTrailerSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTrailerSegmentViews(mutableListOf<PendingTrailerSegment>(item, item)).size, "views")
    }
}
