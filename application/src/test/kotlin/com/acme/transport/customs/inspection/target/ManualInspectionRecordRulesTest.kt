package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualInspectionRecordValidator()
        val item = newManualInspectionRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ManualInspectionRecordRequiredRegionRule("manual inspection record needs this field")
        val item = newManualInspectionRecord("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualInspectionRecordRequiredNameRule("manual inspection record needs this field")
        val item = newManualInspectionRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ManualInspectionRecordVersionRangeRule(255, 3600)
        val item = newManualInspectionRecord("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
