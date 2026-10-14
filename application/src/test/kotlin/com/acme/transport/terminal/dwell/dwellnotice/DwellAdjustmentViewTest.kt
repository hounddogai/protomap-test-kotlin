package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellAdjustment("code-470", "name-78")
        val view = toDwellAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellAdjustmentViews(mutableListOf<DwellAdjustment>(item, item)).size, "views")
    }
}
