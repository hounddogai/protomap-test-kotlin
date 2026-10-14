package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRateCardRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedRateCardRollup("code-128", "label-380")
        assertEquals(CappedRateCardRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedRateCardRollup("code-128", "label-380")
        try {
            item.moveTo(CappedRateCardRollupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRateCardRollupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedRateCardRollup("code-128", "label-380")
        item.status = CappedRateCardRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedRateCardRollupStatus.REQUESTED) }
    }
}
