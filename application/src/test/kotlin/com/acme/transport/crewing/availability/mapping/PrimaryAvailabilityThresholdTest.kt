package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAvailabilityThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryAvailabilityThreshold("code-930", "label-618")
        assertEquals(PrimaryAvailabilityThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryAvailabilityThreshold("code-930", "label-618")
        try {
            item.moveTo(PrimaryAvailabilityThresholdStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAvailabilityThresholdStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryAvailabilityThreshold("code-930", "label-618")
        item.status = PrimaryAvailabilityThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryAvailabilityThresholdStatus.PROPOSED) }
    }
}
