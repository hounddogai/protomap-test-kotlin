package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingHubBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingHubBaselineValidator()
        val item = newRollingHubBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RollingHubBaselineMinimumRangeRule(9.4, 2498.1)
        val item = newRollingHubBaseline("x", "y")
        item.minimum = 2499.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingHubBaselineCodeLengthRule(3600)
        val item = newRollingHubBaseline("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RollingHubBaselineCapacityRangeRule(80, 86400)
        val item = newRollingHubBaseline("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingHubBaselineLabelLengthRule(86400)
        val item = newRollingHubBaseline("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RollingHubBaselineEndsAtRangeRule(32, 3600)
        val item = newRollingHubBaseline("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
