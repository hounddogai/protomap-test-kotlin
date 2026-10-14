package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEvChargeClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackEvChargeClaim("code-256", "title-468")
        assertEquals(FallbackEvChargeClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackEvChargeClaim("code-256", "title-468")
        try {
            item.moveTo(FallbackEvChargeClaimStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEvChargeClaimStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackEvChargeClaim("code-256", "title-468")
        item.status = FallbackEvChargeClaimStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackEvChargeClaimStatus.IDLE) }
    }
}
