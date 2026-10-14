package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireEstimate("code-976", "label-581")
        assertEquals(TireEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTireEstimate("code-976", "label-581")
        try {
            item.moveTo(TireEstimateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireEstimateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireEstimate("code-976", "label-581")
        item.status = TireEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireEstimateStatus.PROPOSED) }
    }
}
