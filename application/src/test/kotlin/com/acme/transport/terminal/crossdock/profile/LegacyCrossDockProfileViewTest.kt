package com.acme.transport.terminal.crossdock.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCrossDockProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCrossDockProfile("code-329", "label-330")
        val view = toLegacyCrossDockProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCrossDockProfileViews(mutableListOf<LegacyCrossDockProfile>(item, item)).size, "views")
    }
}
