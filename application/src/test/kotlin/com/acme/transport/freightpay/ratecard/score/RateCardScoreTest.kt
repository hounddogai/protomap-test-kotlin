package com.acme.transport.freightpay.ratecard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardScoreTest {
    @Test
    fun `starts in open`() {
        val item = newRateCardScore("code-497", "label-66")
        assertEquals(RateCardScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRateCardScore("code-497", "label-66")
        try {
            item.moveTo(RateCardScoreStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardScoreStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRateCardScore("code-497", "label-66")
        item.status = RateCardScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(RateCardScoreStatus.OPEN) }
    }
}
