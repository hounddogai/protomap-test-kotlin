package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRateCardBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalRateCardBaseline("code-385", "label-510")
        assertEquals(GlobalRateCardBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalRateCardBaseline("code-385", "label-510")
        try {
            item.moveTo(GlobalRateCardBaselineStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRateCardBaselineStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalRateCardBaseline("code-385", "label-510")
        item.status = GlobalRateCardBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalRateCardBaselineStatus.DRAFT) }
    }
}
