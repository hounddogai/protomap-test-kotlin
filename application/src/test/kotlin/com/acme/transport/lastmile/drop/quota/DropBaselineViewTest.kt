package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class DropBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropBaseline("code-685", "label-774")
        val view = toDropBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropBaselineViews(mutableListOf<DropBaseline>(item, item)).size, "views")
    }
}
