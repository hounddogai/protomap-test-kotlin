package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingSortationSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingSortationSegment("code-402", "name-193")
        val view = toPendingSortationSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingSortationSegmentViews(mutableListOf<PendingSortationSegment>(item, item)).size, "views")
    }
}
