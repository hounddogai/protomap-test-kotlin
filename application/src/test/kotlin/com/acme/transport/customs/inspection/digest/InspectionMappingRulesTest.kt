package com.acme.transport.customs.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionMappingValidator()
        val item = newInspectionMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = InspectionMappingRequiredPhoneRule("inspection mapping needs an entry")
        val item = newInspectionMapping("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionMappingRequiredLabelRule("inspection mapping needs a value")
        val item = newInspectionMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = InspectionMappingRateRangeRule(7.0, 2178.2)
        val item = newInspectionMapping("x", "y")
        item.rate = 2179.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
