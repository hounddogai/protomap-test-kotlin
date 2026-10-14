package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveZoneAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveZoneAllocation("code-582", "name-290")
        val view = toAdaptiveZoneAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveZoneAllocationViews(mutableListOf<AdaptiveZoneAllocation>(item, item)).size, "views")
    }
}
