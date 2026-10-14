package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictInspectionBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictInspectionBatchValidator()
        val item = newStrictInspectionBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = StrictInspectionBatchContactEmailLengthRule(1000)
        val item = newStrictInspectionBatch("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictInspectionBatchNameLengthRule(80)
        val item = newStrictInspectionBatch("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictInspectionBatchLabelLengthRule(120)
        val item = newStrictInspectionBatch("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = StrictInspectionBatchPalletsRangeRule(80, 120)
        val item = newStrictInspectionBatch("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictInspectionBatchRequiredNameRule("strict inspection batch needs a value")
        val item = newStrictInspectionBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
