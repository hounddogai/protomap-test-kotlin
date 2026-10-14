package com.acme.transport.network.node.nodepolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeBundle("code-264", "label-451")
        val view = toNodeBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeBundleViews(mutableListOf<NodeBundle>(item, item)).size, "views")
    }
}
