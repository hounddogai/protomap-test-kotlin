package com.acme.transport.lastmile.redelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRedeliverySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRedeliverySnapshot("code-752", "name-877")
        val view = toGlobalRedeliverySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRedeliverySnapshotViews(mutableListOf<GlobalRedeliverySnapshot>(item, item)).size, "views")
    }
}
