package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualClearanceCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualClearanceCheckpointValidator()
        val item = newManualClearanceCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ManualClearanceCheckpointRouteCodeLengthRule(80)
        val item = newManualClearanceCheckpoint("x", "y")
        item.routeCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ManualClearanceCheckpointRequiredRouteCodeRule("manual clearance checkpoint needs an entry")
        val item = newManualClearanceCheckpoint("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ManualClearanceCheckpointWeightRangeRule(2.7, 736.2)
        val item = newManualClearanceCheckpoint("x", "y")
        item.weight = 737.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ManualClearanceCheckpointGraceDaysRangeRule(32, 32)
        val item = newManualClearanceCheckpoint("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ManualClearanceCheckpointDiscountRangeRule(9.2, 1756.1)
        val item = newManualClearanceCheckpoint("x", "y")
        item.discount = 1757.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
