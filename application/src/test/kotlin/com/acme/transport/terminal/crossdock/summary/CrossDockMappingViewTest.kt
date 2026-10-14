package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockMapping("code-682", "title-690")
        val view = toCrossDockMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockMappingViews(mutableListOf<CrossDockMapping>(item, item)).size, "views")
    }
}
