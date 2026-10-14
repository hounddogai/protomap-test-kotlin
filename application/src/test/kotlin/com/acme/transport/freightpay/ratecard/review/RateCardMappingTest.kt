package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newRateCardMapping("code-94", "name-457")
        assertEquals(RateCardMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRateCardMapping("code-94", "name-457")
        try {
            item.moveTo(RateCardMappingStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardMappingStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRateCardMapping("code-94", "name-457")
        item.status = RateCardMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RateCardMappingStatus.PROPOSED) }
    }
}
