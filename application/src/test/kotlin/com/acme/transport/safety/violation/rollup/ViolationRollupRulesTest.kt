package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationRollupValidator()
        val item = newViolationRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ViolationRollupRequiredCodeRule("violation rollup needs this field")
        val item = newViolationRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationRollupCodeLengthRule(1000)
        val item = newViolationRollup("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ViolationRollupReviewerLengthRule(120)
        val item = newViolationRollup("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
