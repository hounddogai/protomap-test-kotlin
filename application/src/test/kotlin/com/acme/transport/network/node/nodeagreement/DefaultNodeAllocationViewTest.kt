package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNodeAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultNodeAllocation("code-749", "title-559")
        val view = toDefaultNodeAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultNodeAllocationViews(mutableListOf<DefaultNodeAllocation>(item, item)).size, "views")
    }
}
