package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRateCardRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredRateCardRequest("code-395", "name-701")
        assertEquals(TieredRateCardRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredRateCardRequest("code-395", "name-701")
        try {
            item.moveTo(TieredRateCardRequestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRateCardRequestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredRateCardRequest("code-395", "name-701")
        item.status = TieredRateCardRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredRateCardRequestStatus.REQUESTED) }
    }
}
