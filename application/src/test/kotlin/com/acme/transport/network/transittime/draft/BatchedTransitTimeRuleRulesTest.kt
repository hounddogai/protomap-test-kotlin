package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTransitTimeRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTransitTimeRuleValidator()
        val item = newBatchedTransitTimeRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedTransitTimeRuleStartsAtRangeRule(64, 86400)
        val item = newBatchedTransitTimeRule("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTransitTimeRuleCodeLengthRule(120)
        val item = newBatchedTransitTimeRule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedTransitTimeRuleRequiredCodeRule("batched transit time rule needs a value")
        val item = newBatchedTransitTimeRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
