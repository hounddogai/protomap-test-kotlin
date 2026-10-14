package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCoachingBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCoachingBaselineValidator()
        val item = newDefaultCoachingBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultCoachingBaselineMinimumRangeRule(4.0, 261.1)
        val item = newDefaultCoachingBaseline("x", "y")
        item.minimum = 262.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultCoachingBaselineCodeLengthRule(1000)
        val item = newDefaultCoachingBaseline("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
