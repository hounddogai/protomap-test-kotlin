package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newRateCardVariance("code-807", "name-343")
        assertEquals(RateCardVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRateCardVariance("code-807", "name-343")
        try {
            item.moveTo(RateCardVarianceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardVarianceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRateCardVariance("code-807", "name-343")
        item.status = RateCardVarianceStatus.FAILED
        assertFails("move") { item.moveTo(RateCardVarianceStatus.NEW) }
    }
}
