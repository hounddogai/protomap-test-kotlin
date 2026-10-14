package com.acme.transport.network.servicearea.areaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedServiceAreaClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedServiceAreaClaim("code-89", "name-356")
        assertEquals(WeightedServiceAreaClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newWeightedServiceAreaClaim("code-89", "name-356")
        try {
            item.moveTo(WeightedServiceAreaClaimStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedServiceAreaClaimStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedServiceAreaClaim("code-89", "name-356")
        item.status = WeightedServiceAreaClaimStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedServiceAreaClaimStatus.IDLE) }
    }
}
