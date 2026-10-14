package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockRequest("code-646", "label-587")
        val view = toCrossDockRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockRequestViews(mutableListOf<CrossDockRequest>(item, item)).size, "views")
    }
}
