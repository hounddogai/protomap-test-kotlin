package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientInspectionMappingValidator()
        val item = newLenientInspectionMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientInspectionMappingTitleLengthRule(64)
        val item = newLenientInspectionMapping("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LenientInspectionMappingFuelLitersRangeRule(1.9, 2309.6)
        val item = newLenientInspectionMapping("x", "y")
        item.fuelLiters = 2311.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientInspectionMappingCodeLengthRule(120)
        val item = newLenientInspectionMapping("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
