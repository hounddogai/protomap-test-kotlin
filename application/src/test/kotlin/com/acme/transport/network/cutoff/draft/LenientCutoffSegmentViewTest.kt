package com.acme.transport.network.cutoff.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCutoffSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCutoffSegment("code-264", "label-23")
        val view = toLenientCutoffSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCutoffSegmentViews(mutableListOf<LenientCutoffSegment>(item, item)).size, "views")
    }
}
