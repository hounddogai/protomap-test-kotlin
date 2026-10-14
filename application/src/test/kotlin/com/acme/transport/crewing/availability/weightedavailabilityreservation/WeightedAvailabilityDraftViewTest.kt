package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAvailabilityDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAvailabilityDraft("code-828", "name-416")
        val view = toWeightedAvailabilityDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAvailabilityDraftViews(mutableListOf<WeightedAvailabilityDraft>(item, item)).size, "views")
    }
}
