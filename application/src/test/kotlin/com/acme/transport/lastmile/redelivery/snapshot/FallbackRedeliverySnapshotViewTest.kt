package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRedeliverySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRedeliverySnapshot("code-230", "name-354")
        val view = toFallbackRedeliverySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRedeliverySnapshotViews(mutableListOf<FallbackRedeliverySnapshot>(item, item)).size, "views")
    }
}
