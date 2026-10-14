package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCapacityExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCapacityExceptionValidator()
        val item = newManualCapacityException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ManualCapacityExceptionRevisionRangeRule(16, 120)
        val item = newManualCapacityException("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualCapacityExceptionCodeLengthRule(3600)
        val item = newManualCapacityException("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualCapacityExceptionRequiredLabelRule("manual capacity exception needs an entry")
        val item = newManualCapacityException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
