package com.acme.transport.freightpay.dispute.disputeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDisputeRuleTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalDisputeRule("code-25", "label-439")
        assertEquals(RegionalDisputeRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalDisputeRule("code-25", "label-439")
        try {
            item.moveTo(RegionalDisputeRuleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDisputeRuleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalDisputeRule("code-25", "label-439")
        item.status = RegionalDisputeRuleStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalDisputeRuleStatus.CREATED) }
    }
}
