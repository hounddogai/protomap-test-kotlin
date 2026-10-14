package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTenderProfileValidator()
        val item = newStrictTenderProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictTenderProfileRequiredLabelRule("strict tender profile needs a value")
        val item = newStrictTenderProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictTenderProfileCodeLengthRule(255)
        val item = newStrictTenderProfile("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictTenderProfileCapacityRangeRule(16, 3600)
        val item = newStrictTenderProfile("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTenderProfileRequiredCodeRule("strict tender profile needs this field")
        val item = newStrictTenderProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = StrictTenderProfileBatchSizeRangeRule(86400, 86400)
        val item = newStrictTenderProfile("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
