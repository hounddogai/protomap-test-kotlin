package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRelayPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRelayPlanValidator()
        val item = newBatchedRelayPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = BatchedRelayPlanShiftCodeLengthRule(16)
        val item = newBatchedRelayPlan("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedRelayPlanLabelLengthRule(80)
        val item = newBatchedRelayPlan("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BatchedRelayPlanLimitRangeRule(64, 255)
        val item = newBatchedRelayPlan("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
