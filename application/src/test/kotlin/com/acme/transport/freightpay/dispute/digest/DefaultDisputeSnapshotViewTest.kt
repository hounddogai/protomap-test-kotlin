package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDisputeSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDisputeSnapshot("code-550", "label-873")
        val view = toDefaultDisputeSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDisputeSnapshotViews(mutableListOf<DefaultDisputeSnapshot>(item, item)).size, "views")
    }
}
