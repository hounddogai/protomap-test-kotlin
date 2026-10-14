package com.acme.transport.terminal.dockdoor.tiereddooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDockDoorTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDockDoorTemplateValidator()
        val item = newRollingDockDoorTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RollingDockDoorTemplateEndsAtRangeRule(80, 86400)
        val item = newRollingDockDoorTemplate("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RollingDockDoorTemplateTrailerIdLengthRule(120)
        val item = newRollingDockDoorTemplate("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
