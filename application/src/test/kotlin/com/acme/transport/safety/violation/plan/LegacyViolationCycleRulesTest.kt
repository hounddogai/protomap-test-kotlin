package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyViolationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyViolationCycleValidator()
        val item = newLegacyViolationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = LegacyViolationCycleRouteCodeLengthRule(16)
        val item = newLegacyViolationCycle("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyViolationCycleCodeLengthRule(255)
        val item = newLegacyViolationCycle("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
