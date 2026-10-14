package com.acme.transport.terminal.crossdock.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockException("code-334", "name-582")
        val view = toCrossDockExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockExceptionViews(mutableListOf<CrossDockException>(item, item)).size, "views")
    }
}
