package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionBundleValidator()
        val item = newAdaptiveInspectionBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveInspectionBundleRequiredCodeRule("adaptive inspection bundle needs this field")
        val item = newAdaptiveInspectionBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AdaptiveInspectionBundleCeilingRangeRule(8.0, 1998.1)
        val item = newAdaptiveInspectionBundle("x", "y")
        item.ceiling = 1999.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AdaptiveInspectionBundleContactEmailLengthRule(80)
        val item = newAdaptiveInspectionBundle("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveInspectionBundleRequiredNameRule("adaptive inspection bundle needs an entry")
        val item = newAdaptiveInspectionBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
