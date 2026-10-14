package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCutoffSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCutoffSegment("code-46", "name-155")
        val view = toPendingCutoffSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCutoffSegmentViews(mutableListOf<PendingCutoffSegment>(item, item)).size, "views")
    }
}
