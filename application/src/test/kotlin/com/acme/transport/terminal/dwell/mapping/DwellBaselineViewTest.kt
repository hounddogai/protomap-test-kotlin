package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellBaseline("code-736", "title-107")
        val view = toDwellBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellBaselineViews(mutableListOf<DwellBaseline>(item, item)).size, "views")
    }
}
