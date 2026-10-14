package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingNodeTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingNodeTier("code-886", "name-278")
        val view = toRollingNodeTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingNodeTierViews(mutableListOf<RollingNodeTier>(item, item)).size, "views")
    }
}
