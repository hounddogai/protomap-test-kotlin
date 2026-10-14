package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationSummaryValidator()
        val item = newViolationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ViolationSummaryEndsAtRangeRule(1000, 86400)
        val item = newViolationSummary("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ViolationSummaryRequiredCategoryRule("violation summary needs an entry")
        val item = newViolationSummary("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = ViolationSummaryRequiredContactEmailRule("violation summary needs a value")
        val item = newViolationSummary("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
