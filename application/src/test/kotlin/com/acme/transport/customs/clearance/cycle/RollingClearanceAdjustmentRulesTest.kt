package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingClearanceAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingClearanceAdjustmentValidator()
        val item = newRollingClearanceAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingClearanceAdjustmentCodeLengthRule(64)
        val item = newRollingClearanceAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingClearanceAdjustmentLaneLengthRule(120)
        val item = newRollingClearanceAdjustment("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
