package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAvailabilityMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAvailabilityMapping("code-373", "title-655")
        val view = toAdaptiveAvailabilityMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAvailabilityMappingViews(mutableListOf<AdaptiveAvailabilityMapping>(item, item)).size, "views")
    }
}
