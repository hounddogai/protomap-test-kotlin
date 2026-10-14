package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newRateCardWindow("code-248", "name-417")
        assertEquals(RateCardWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRateCardWindow("code-248", "name-417")
        try {
            item.moveTo(RateCardWindowStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardWindowStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRateCardWindow("code-248", "name-417")
        item.status = RateCardWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(RateCardWindowStatus.REQUESTED) }
    }
}
