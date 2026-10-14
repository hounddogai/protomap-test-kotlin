package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedServicePlanEntryTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedServicePlanEntry("code-581", "name-383")
        assertEquals(WeightedServicePlanEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedServicePlanEntry("code-581", "name-383")
        try {
            item.moveTo(WeightedServicePlanEntryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedServicePlanEntryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedServicePlanEntry("code-581", "name-383")
        item.status = WeightedServicePlanEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedServicePlanEntryStatus.OPEN) }
    }
}
