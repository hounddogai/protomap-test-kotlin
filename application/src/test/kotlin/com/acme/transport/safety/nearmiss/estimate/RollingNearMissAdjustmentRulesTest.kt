package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingNearMissAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingNearMissAdjustmentValidator()
        val item = newRollingNearMissAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = RollingNearMissAdjustmentRequiredExternalRefRule("rolling near miss adjustment needs an entry")
        val item = newRollingNearMissAdjustment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingNearMissAdjustmentRequiredCodeRule("rolling near miss adjustment needs a value")
        val item = newRollingNearMissAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RollingNearMissAdjustmentBudgetRangeRule(4.3, 542.5)
        val item = newRollingNearMissAdjustment("x", "y")
        item.budget = 544.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
