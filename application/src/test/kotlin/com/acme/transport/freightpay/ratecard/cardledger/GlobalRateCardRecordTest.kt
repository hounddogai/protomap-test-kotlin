package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRateCardRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalRateCardRecord("code-945", "label-618")
        assertEquals(GlobalRateCardRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalRateCardRecord("code-945", "label-618")
        try {
            item.moveTo(GlobalRateCardRecordStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRateCardRecordStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalRateCardRecord("code-945", "label-618")
        item.status = GlobalRateCardRecordStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalRateCardRecordStatus.DRAFT) }
    }
}
