package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newRateCardBaseline("code-390", "label-569")
        assertEquals(RateCardBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRateCardBaseline("code-390", "label-569")
        try {
            item.moveTo(RateCardBaselineStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardBaselineStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRateCardBaseline("code-390", "label-569")
        item.status = RateCardBaselineStatus.FAILED
        assertFails("move") { item.moveTo(RateCardBaselineStatus.NEW) }
    }
}
