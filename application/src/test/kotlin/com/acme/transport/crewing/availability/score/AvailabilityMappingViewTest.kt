package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityMapping("code-871", "name-971")
        val view = toAvailabilityMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityMappingViews(mutableListOf<AvailabilityMapping>(item, item)).size, "views")
    }
}
