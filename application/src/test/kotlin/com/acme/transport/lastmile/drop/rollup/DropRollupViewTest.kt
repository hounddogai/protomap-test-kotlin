package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class DropRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropRollup("code-514", "name-870")
        val view = toDropRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropRollupViews(mutableListOf<DropRollup>(item, item)).size, "views")
    }
}
