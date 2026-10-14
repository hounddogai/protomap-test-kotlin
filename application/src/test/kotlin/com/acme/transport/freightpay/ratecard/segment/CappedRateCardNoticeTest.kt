package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRateCardNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newCappedRateCardNotice("code-240", "title-732")
        assertEquals(CappedRateCardNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedRateCardNotice("code-240", "title-732")
        try {
            item.moveTo(CappedRateCardNoticeStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRateCardNoticeStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedRateCardNotice("code-240", "title-732")
        item.status = CappedRateCardNoticeStatus.FAILED
        assertFails("move") { item.moveTo(CappedRateCardNoticeStatus.NEW) }
    }
}
