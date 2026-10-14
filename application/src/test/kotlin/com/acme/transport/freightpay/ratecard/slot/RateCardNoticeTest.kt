package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newRateCardNotice("code-349", "name-709")
        assertEquals(RateCardNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRateCardNotice("code-349", "name-709")
        try {
            item.moveTo(RateCardNoticeStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardNoticeStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRateCardNotice("code-349", "name-709")
        item.status = RateCardNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(RateCardNoticeStatus.IDLE) }
    }
}
