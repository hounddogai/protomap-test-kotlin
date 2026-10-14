package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAvailabilityMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingAvailabilityMapping("code-472", "name-182")
        val view = toRollingAvailabilityMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingAvailabilityMappingViews(mutableListOf<RollingAvailabilityMapping>(item, item)).size, "views")
    }
}
