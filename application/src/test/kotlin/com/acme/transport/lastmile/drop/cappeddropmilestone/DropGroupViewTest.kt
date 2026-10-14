package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class DropGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropGroup("code-168", "name-996")
        val view = toDropGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropGroupViews(mutableListOf<DropGroup>(item, item)).size, "views")
    }
}
