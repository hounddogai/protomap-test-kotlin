package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRateCardDigestTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedRateCardDigest("code-505", "title-970")
        assertEquals(WeightedRateCardDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newWeightedRateCardDigest("code-505", "title-970")
        try {
            item.moveTo(WeightedRateCardDigestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRateCardDigestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedRateCardDigest("code-505", "title-970")
        item.status = WeightedRateCardDigestStatus.FAILED
        assertFails("move") { item.moveTo(WeightedRateCardDigestStatus.NEW) }
    }
}
