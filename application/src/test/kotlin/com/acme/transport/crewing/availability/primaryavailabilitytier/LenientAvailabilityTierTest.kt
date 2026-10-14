package com.acme.transport.crewing.availability.primaryavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAvailabilityTierTest {
    @Test
    fun `starts in created`() {
        val item = newLenientAvailabilityTier("code-745", "name-495")
        assertEquals(LenientAvailabilityTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientAvailabilityTier("code-745", "name-495")
        try {
            item.moveTo(LenientAvailabilityTierStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAvailabilityTierStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientAvailabilityTier("code-745", "name-495")
        item.status = LenientAvailabilityTierStatus.RETIRED
        assertFails("move") { item.moveTo(LenientAvailabilityTierStatus.CREATED) }
    }
}
