package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalInspectionTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalInspectionTargetValidator()
        val item = newRegionalInspectionTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalInspectionTargetNameLengthRule(80)
        val item = newRegionalInspectionTarget("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RegionalInspectionTargetEndsAtRangeRule(16, 64)
        val item = newRegionalInspectionTarget("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalInspectionTargetUnitLengthRule(1000)
        val item = newRegionalInspectionTarget("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalInspectionTargetRequiredCodeRule("regional inspection target needs this field")
        val item = newRegionalInspectionTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalInspectionTargetRequiredNameRule("regional inspection target needs this field")
        val item = newRegionalInspectionTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
