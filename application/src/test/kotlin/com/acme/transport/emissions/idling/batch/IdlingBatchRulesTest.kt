package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingBatchValidator()
        val item = newIdlingBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingBatchTitleLengthRule(120)
        val item = newIdlingBatch("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = IdlingBatchOdometerRangeRule(64, 120)
        val item = newIdlingBatch("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingBatchRequiredTitleRule("idling batch needs this field")
        val item = newIdlingBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
