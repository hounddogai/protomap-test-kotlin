package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRateCardCycleTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultRateCardCycle("code-172", "name-497")
        assertEquals(DefaultRateCardCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultRateCardCycle("code-172", "name-497")
        try {
            item.moveTo(DefaultRateCardCycleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRateCardCycleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultRateCardCycle("code-172", "name-497")
        item.status = DefaultRateCardCycleStatus.FAILED
        assertFails("move") { item.moveTo(DefaultRateCardCycleStatus.NEW) }
    }
}
