package com.acme.transport.network.node.weightednodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeRecord("code-539", "name-413")
        val view = toNodeRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeRecordViews(mutableListOf<NodeRecord>(item, item)).size, "views")
    }
}
