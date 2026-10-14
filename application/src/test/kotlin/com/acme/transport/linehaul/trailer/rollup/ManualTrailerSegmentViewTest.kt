package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTrailerSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTrailerSegment("code-757", "label-518")
        val view = toManualTrailerSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTrailerSegmentViews(mutableListOf<ManualTrailerSegment>(item, item)).size, "views")
    }
}
