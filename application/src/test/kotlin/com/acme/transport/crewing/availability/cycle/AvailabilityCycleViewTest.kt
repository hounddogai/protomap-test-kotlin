package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityCycle("code-48", "name-690")
        val view = toAvailabilityCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityCycleViews(mutableListOf<AvailabilityCycle>(item, item)).size, "views")
    }
}
