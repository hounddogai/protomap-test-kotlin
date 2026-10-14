package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellEntry("code-163", "name-139")
        val view = toDwellEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellEntryViews(mutableListOf<DwellEntry>(item, item)).size, "views")
    }
}
