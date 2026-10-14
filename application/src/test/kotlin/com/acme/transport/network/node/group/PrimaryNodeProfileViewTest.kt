package com.acme.transport.network.node.group

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryNodeProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryNodeProfile("code-330", "name-855")
        val view = toPrimaryNodeProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryNodeProfileViews(mutableListOf<PrimaryNodeProfile>(item, item)).size, "views")
    }
}
