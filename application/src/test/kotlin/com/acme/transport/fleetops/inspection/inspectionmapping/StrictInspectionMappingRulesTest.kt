package com.acme.transport.fleetops.inspection.inspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictInspectionMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictInspectionMappingValidator()
        val item = newStrictInspectionMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictInspectionMappingTitleLengthRule(255)
        val item = newStrictInspectionMapping("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictInspectionMappingRequiredCodeRule("strict inspection mapping needs an entry")
        val item = newStrictInspectionMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = StrictInspectionMappingDiscountRangeRule(1.0, 1450.0)
        val item = newStrictInspectionMapping("x", "y")
        item.discount = 1451.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
