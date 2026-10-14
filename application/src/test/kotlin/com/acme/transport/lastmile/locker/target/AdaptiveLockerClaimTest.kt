package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLockerClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveLockerClaim("code-999", "label-914")
        assertEquals(AdaptiveLockerClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveLockerClaim("code-999", "label-914")
        try {
            item.moveTo(AdaptiveLockerClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLockerClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveLockerClaim("code-999", "label-914")
        item.status = AdaptiveLockerClaimStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveLockerClaimStatus.IDLE) }
    }
}
