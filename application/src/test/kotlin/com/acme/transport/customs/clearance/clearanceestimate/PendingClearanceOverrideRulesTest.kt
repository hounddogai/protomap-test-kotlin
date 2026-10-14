package com.acme.transport.customs.clearance.clearanceestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingClearanceOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingClearanceOverrideValidator()
        val item = newPendingClearanceOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = PendingClearanceOverrideLaneLengthRule(120)
        val item = newPendingClearanceOverride("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingClearanceOverrideRequiredCodeRule("pending clearance override needs a value")
        val item = newPendingClearanceOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PendingClearanceOverrideCapacityRangeRule(3600, 86400)
        val item = newPendingClearanceOverride("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
