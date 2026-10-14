package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAvailabilityDraftTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedAvailabilityDraft("code-828", "name-416")
        assertEquals(WeightedAvailabilityDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newWeightedAvailabilityDraft("code-828", "name-416")
        try {
            item.moveTo(WeightedAvailabilityDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAvailabilityDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedAvailabilityDraft("code-828", "name-416")
        item.status = WeightedAvailabilityDraftStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedAvailabilityDraftStatus.CREATED) }
    }
}
