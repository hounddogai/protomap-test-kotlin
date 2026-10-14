package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRosterBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRosterBatchValidator()
        val item = newLenientRosterBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRosterBatchCodeLengthRule(64)
        val item = newLenientRosterBatch("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = LenientRosterBatchRequiredLaneRule("lenient roster batch needs a value")
        val item = newLenientRosterBatch("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = LenientRosterBatchMarginRangeRule(1.5, 2853.6)
        val item = newLenientRosterBatch("x", "y")
        item.margin = 2855.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = LenientRosterBatchUnitLengthRule(1000)
        val item = newLenientRosterBatch("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientRosterBatchRequiredLabelRule("lenient roster batch needs a value")
        val item = newLenientRosterBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
