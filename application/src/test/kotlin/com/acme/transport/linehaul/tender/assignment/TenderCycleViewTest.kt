package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderCycle("code-547", "title-540")
        val view = toTenderCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderCycleViews(mutableListOf<TenderCycle>(item, item)).size, "views")
    }
}
