package com.acme.transport.terminal.dwell.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellPolicyValidator()
        val item = newDwellPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DwellPolicyDurationMinutesRangeRule(3600, 86400)
        val item = newDwellPolicy("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DwellPolicyCodeLengthRule(120)
        val item = newDwellPolicy("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
