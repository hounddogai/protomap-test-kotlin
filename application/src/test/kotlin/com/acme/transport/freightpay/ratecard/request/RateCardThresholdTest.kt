package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newRateCardThreshold("code-3", "title-703")
        assertEquals(RateCardThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRateCardThreshold("code-3", "title-703")
        try {
            item.moveTo(RateCardThresholdStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardThresholdStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRateCardThreshold("code-3", "title-703")
        item.status = RateCardThresholdStatus.FAILED
        assertFails("move") { item.moveTo(RateCardThresholdStatus.NEW) }
    }
}
