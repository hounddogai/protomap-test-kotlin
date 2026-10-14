package com.acme.transport.network.node.nodepolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryNodePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryNodePolicy("code-608", "title-256")
        val view = toPrimaryNodePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryNodePolicyViews(mutableListOf<PrimaryNodePolicy>(item, item)).size, "views")
    }
}
