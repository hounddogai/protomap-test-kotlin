package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDisputeClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalDisputeClaim("code-246", "name-309")
        assertEquals(SeasonalDisputeClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalDisputeClaim("code-246", "name-309")
        try {
            item.moveTo(SeasonalDisputeClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDisputeClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalDisputeClaim("code-246", "name-309")
        item.status = SeasonalDisputeClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalDisputeClaimStatus.REQUESTED) }
    }
}
