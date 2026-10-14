package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerSegment("code-348", "title-677")
        val view = toTrailerSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerSegmentViews(mutableListOf<TrailerSegment>(item, item)).size, "views")
    }
}
