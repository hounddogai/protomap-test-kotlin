package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellMapping("code-384", "title-597")
        val view = toDwellMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellMappingViews(mutableListOf<DwellMapping>(item, item)).size, "views")
    }
}
