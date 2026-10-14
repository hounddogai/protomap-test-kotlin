package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderAllocation("code-792", "label-289")
        val view = toTenderAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderAllocationViews(mutableListOf<TenderAllocation>(item, item)).size, "views")
    }
}
