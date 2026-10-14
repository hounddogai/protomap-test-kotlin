package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCrossDockExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCrossDockException("code-268", "title-239")
        val view = toLegacyCrossDockExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCrossDockExceptionViews(mutableListOf<LegacyCrossDockException>(item, item)).size, "views")
    }
}
