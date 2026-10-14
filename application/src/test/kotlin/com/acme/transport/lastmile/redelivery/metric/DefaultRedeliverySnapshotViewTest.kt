package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRedeliverySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRedeliverySnapshot("code-569", "title-307")
        val view = toDefaultRedeliverySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRedeliverySnapshotViews(mutableListOf<DefaultRedeliverySnapshot>(item, item)).size, "views")
    }
}
