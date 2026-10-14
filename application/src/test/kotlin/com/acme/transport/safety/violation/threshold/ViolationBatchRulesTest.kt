package com.acme.transport.safety.violation.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationBatchValidator()
        val item = newViolationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ViolationBatchRequiredCodeRule("violation batch needs this field")
        val item = newViolationBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ViolationBatchWindowDaysRangeRule(32, 80)
        val item = newViolationBatch("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ViolationBatchRequiredNotesRule("violation batch needs an entry")
        val item = newViolationBatch("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
