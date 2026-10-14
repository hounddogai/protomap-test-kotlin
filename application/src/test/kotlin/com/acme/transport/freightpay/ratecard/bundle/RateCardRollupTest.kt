package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newRateCardRollup("code-942", "label-936")
        assertEquals(RateCardRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRateCardRollup("code-942", "label-936")
        try {
            item.moveTo(RateCardRollupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardRollupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRateCardRollup("code-942", "label-936")
        item.status = RateCardRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(RateCardRollupStatus.REQUESTED) }
    }
}
