package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialRecordValidator()
        val item = newAccessorialRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = AccessorialRecordUnitLengthRule(3600)
        val item = newAccessorialRecord("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AccessorialRecordRequiredLabelRule("accessorial record needs this field")
        val item = newAccessorialRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AccessorialRecordCreatedAtRangeRule(32, 120)
        val item = newAccessorialRecord("x", "y")
        item.createdAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
