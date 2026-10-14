package com.acme.transport.freightpay.ratecard.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRateCardVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackRateCardVariance("code-643", "title-395")
        assertEquals(FallbackRateCardVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackRateCardVariance("code-643", "title-395")
        try {
            item.moveTo(FallbackRateCardVarianceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRateCardVarianceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackRateCardVariance("code-643", "title-395")
        item.status = FallbackRateCardVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackRateCardVarianceStatus.IDLE) }
    }
}
