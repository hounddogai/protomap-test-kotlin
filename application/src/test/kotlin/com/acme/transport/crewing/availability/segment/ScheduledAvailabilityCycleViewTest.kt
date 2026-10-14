package com.acme.transport.crewing.availability.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAvailabilityCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAvailabilityCycle("code-697", "name-332")
        val view = toScheduledAvailabilityCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAvailabilityCycleViews(mutableListOf<ScheduledAvailabilityCycle>(item, item)).size, "views")
    }
}
