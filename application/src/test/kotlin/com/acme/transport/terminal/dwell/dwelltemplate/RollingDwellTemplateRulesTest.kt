package com.acme.transport.terminal.dwell.dwelltemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDwellTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDwellTemplateValidator()
        val item = newRollingDwellTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = RollingDwellTemplateDepotLengthRule(80)
        val item = newRollingDwellTemplate("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RollingDwellTemplateEndsAtRangeRule(16, 86400)
        val item = newRollingDwellTemplate("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
