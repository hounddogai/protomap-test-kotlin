package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellAllocation("code-456", "label-810")
        val view = toDwellAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellAllocationViews(mutableListOf<DwellAllocation>(item, item)).size, "views")
    }
}
