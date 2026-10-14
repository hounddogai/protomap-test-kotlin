package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRateCardThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictRateCardThreshold("code-991", "label-413")
        assertEquals(StrictRateCardThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictRateCardThreshold("code-991", "label-413")
        try {
            item.moveTo(StrictRateCardThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRateCardThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictRateCardThreshold("code-991", "label-413")
        item.status = StrictRateCardThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictRateCardThresholdStatus.REQUESTED) }
    }
}
