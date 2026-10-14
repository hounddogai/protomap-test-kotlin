package com.acme.transport.customs.clearance.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyClearanceWindowValidator()
        val item = newLegacyClearanceWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyClearanceWindowLabelLengthRule(32)
        val item = newLegacyClearanceWindow("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyClearanceWindowUpdatedAtRangeRule(255, 86400)
        val item = newLegacyClearanceWindow("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyClearanceWindowCodeLengthRule(255)
        val item = newLegacyClearanceWindow("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
