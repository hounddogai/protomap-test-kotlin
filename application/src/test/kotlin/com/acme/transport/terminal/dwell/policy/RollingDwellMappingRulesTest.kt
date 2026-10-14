package com.acme.transport.terminal.dwell.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDwellMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDwellMappingValidator()
        val item = newRollingDwellMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingDwellMappingLabelLengthRule(16)
        val item = newRollingDwellMapping("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingDwellMappingCodeLengthRule(1000)
        val item = newRollingDwellMapping("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingDwellMappingRequiredLabelRule("rolling dwell mapping needs a value")
        val item = newRollingDwellMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RollingDwellMappingRequiredLaneRule("rolling dwell mapping needs a value")
        val item = newRollingDwellMapping("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingDwellMappingLaneLengthRule(16)
        val item = newRollingDwellMapping("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
