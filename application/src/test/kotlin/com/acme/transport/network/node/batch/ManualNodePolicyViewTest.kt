package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualNodePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualNodePolicy("code-261", "title-829")
        val view = toManualNodePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualNodePolicyViews(mutableListOf<ManualNodePolicy>(item, item)).size, "views")
    }
}
