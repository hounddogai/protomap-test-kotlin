package com.acme.transport.lastmile.drop.dropdraft

import kotlin.test.Test
import kotlin.test.assertEquals

class DropOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropOverride("code-584", "label-468")
        val view = toDropOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropOverrideViews(mutableListOf<DropOverride>(item, item)).size, "views")
    }
}
