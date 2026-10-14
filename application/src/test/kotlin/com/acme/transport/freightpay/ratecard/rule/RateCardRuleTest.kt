package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardRuleTest {
    @Test
    fun `starts in new`() {
        val item = newRateCardRule("code-423", "label-614")
        assertEquals(RateCardRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRateCardRule("code-423", "label-614")
        try {
            item.moveTo(RateCardRuleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardRuleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRateCardRule("code-423", "label-614")
        item.status = RateCardRuleStatus.FAILED
        assertFails("move") { item.moveTo(RateCardRuleStatus.NEW) }
    }
}
