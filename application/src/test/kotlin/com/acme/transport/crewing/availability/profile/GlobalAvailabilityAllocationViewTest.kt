package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAvailabilityAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAvailabilityAllocation("code-900", "name-278")
        val view = toGlobalAvailabilityAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAvailabilityAllocationViews(mutableListOf<GlobalAvailabilityAllocation>(item, item)).size, "views")
    }
}
