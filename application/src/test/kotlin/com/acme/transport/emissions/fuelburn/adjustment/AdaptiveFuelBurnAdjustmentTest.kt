package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveFuelBurnAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveFuelBurnAdjustment("code-237", "label-529")
        assertEquals(AdaptiveFuelBurnAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveFuelBurnAdjustment("code-237", "label-529")
        try {
            item.moveTo(AdaptiveFuelBurnAdjustmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveFuelBurnAdjustmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveFuelBurnAdjustment("code-237", "label-529")
        item.status = AdaptiveFuelBurnAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveFuelBurnAdjustmentStatus.REQUESTED) }
    }
}
