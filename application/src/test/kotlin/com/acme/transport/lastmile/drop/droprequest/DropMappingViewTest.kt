package com.acme.transport.lastmile.drop.droprequest

import kotlin.test.Test
import kotlin.test.assertEquals

class DropMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropMapping("code-472", "label-595")
        val view = toDropMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropMappingViews(mutableListOf<DropMapping>(item, item)).size, "views")
    }
}
