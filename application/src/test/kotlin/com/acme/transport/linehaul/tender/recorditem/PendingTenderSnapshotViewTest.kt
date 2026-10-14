package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTenderSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTenderSnapshot("code-260", "label-375")
        val view = toPendingTenderSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTenderSnapshotViews(mutableListOf<PendingTenderSnapshot>(item, item)).size, "views")
    }
}
