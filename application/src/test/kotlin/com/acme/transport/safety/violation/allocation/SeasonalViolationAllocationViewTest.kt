package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalViolationAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalViolationAllocation("code-2", "label-645")
        val view = toSeasonalViolationAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalViolationAllocationViews(mutableListOf<SeasonalViolationAllocation>(item, item)).size, "views")
    }
}
