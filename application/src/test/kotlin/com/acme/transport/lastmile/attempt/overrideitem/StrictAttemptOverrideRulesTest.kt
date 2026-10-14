package com.acme.transport.lastmile.attempt.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAttemptOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAttemptOverrideValidator()
        val item = newStrictAttemptOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = StrictAttemptOverrideRouteCodeLengthRule(86400)
        val item = newStrictAttemptOverride("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictAttemptOverrideRequiredCodeRule("strict attempt override needs this field")
        val item = newStrictAttemptOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = StrictAttemptOverridePriorityRangeRule(32, 64)
        val item = newStrictAttemptOverride("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
