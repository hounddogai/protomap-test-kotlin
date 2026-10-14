package com.acme.transport.crewing.availability.availabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAvailabilityTierTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveAvailabilityTier("code-712", "title-988")
        assertEquals(AdaptiveAvailabilityTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveAvailabilityTier("code-712", "title-988")
        try {
            item.moveTo(AdaptiveAvailabilityTierStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAvailabilityTierStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveAvailabilityTier("code-712", "title-988")
        item.status = AdaptiveAvailabilityTierStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveAvailabilityTierStatus.PLANNED) }
    }
}
