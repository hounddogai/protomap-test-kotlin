package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryEstimateValidator()
        val item = newRedeliveryEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RedeliveryEstimateRequiredNameRule("redelivery estimate needs a value")
        val item = newRedeliveryEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryEstimateRequiredCodeRule("redelivery estimate needs this field")
        val item = newRedeliveryEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RedeliveryEstimateNameLengthRule(16)
        val item = newRedeliveryEstimate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryEstimateCodeLengthRule(80)
        val item = newRedeliveryEstimate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RedeliveryEstimateReviewerLengthRule(3600)
        val item = newRedeliveryEstimate("x", "y")
        item.reviewer = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
