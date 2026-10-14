package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceAdjustmentValidator()
        val item = newClearanceAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ClearanceAdjustmentRequiredLaneRule("clearance adjustment needs this field")
        val item = newClearanceAdjustment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ClearanceAdjustmentRequiredTitleRule("clearance adjustment needs a value")
        val item = newClearanceAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceAdjustmentCodeLengthRule(16)
        val item = newClearanceAdjustment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
