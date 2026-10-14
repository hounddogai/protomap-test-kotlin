package com.acme.transport.crewing.availability.availabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAvailabilityBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAvailabilityBudget("code-746", "label-335")
        val view = toManualAvailabilityBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAvailabilityBudgetViews(mutableListOf<ManualAvailabilityBudget>(item, item)).size, "views")
    }
}
