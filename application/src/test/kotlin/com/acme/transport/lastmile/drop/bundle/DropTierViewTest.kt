package com.acme.transport.lastmile.drop.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class DropTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropTier("code-845", "label-598")
        val view = toDropTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropTierViews(mutableListOf<DropTier>(item, item)).size, "views")
    }
}
