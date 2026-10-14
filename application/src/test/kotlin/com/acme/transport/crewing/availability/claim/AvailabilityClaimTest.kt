package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newAvailabilityClaim("code-236", "label-109")
        assertEquals(AvailabilityClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAvailabilityClaim("code-236", "label-109")
        try {
            item.moveTo(AvailabilityClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAvailabilityClaim("code-236", "label-109")
        item.status = AvailabilityClaimStatus.STOPPED
        assertFails("move") { item.moveTo(AvailabilityClaimStatus.IDLE) }
    }
}
