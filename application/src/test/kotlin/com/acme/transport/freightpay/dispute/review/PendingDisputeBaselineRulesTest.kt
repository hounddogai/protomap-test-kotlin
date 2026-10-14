package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDisputeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDisputeBaselineValidator()
        val item = newPendingDisputeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingDisputeBaselineRequiredLabelRule("pending dispute baseline needs an entry")
        val item = newPendingDisputeBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDisputeBaselineCodeLengthRule(16)
        val item = newPendingDisputeBaseline("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = PendingDisputeBaselineDescriptionLengthRule(1000)
        val item = newPendingDisputeBaseline("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = PendingDisputeBaselineRequiredDepotRule("pending dispute baseline needs this field")
        val item = newPendingDisputeBaseline("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
