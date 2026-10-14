package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAvailabilityGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAvailabilityGroup("code-996", "name-82")
        val view = toWeightedAvailabilityGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAvailabilityGroupViews(mutableListOf<WeightedAvailabilityGroup>(item, item)).size, "views")
    }
}
