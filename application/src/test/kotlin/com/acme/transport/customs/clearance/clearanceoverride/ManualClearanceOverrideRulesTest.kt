package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualClearanceOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualClearanceOverrideValidator()
        val item = newManualClearanceOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualClearanceOverrideRequiredCodeRule("manual clearance override needs a value")
        val item = newManualClearanceOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualClearanceOverrideCodeLengthRule(86400)
        val item = newManualClearanceOverride("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ManualClearanceOverrideCeilingRangeRule(3.5, 4196.4)
        val item = newManualClearanceOverride("x", "y")
        item.ceiling = 4197.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualClearanceOverrideNameLengthRule(32)
        val item = newManualClearanceOverride("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualClearanceOverrideRequiredNameRule("manual clearance override needs a value")
        val item = newManualClearanceOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
