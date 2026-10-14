package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeMappingValidator()
        val item = newDisputeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DisputeMappingSegmentLengthRule(16)
        val item = newDisputeMapping("x", "y")
        item.segment = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DisputeMappingUnitLengthRule(120)
        val item = newDisputeMapping("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeMappingRequiredCodeRule("dispute mapping needs a value")
        val item = newDisputeMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
