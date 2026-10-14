package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityGroup("code-48", "name-621")
        val view = toAvailabilityGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityGroupViews(mutableListOf<AvailabilityGroup>(item, item)).size, "views")
    }
}
