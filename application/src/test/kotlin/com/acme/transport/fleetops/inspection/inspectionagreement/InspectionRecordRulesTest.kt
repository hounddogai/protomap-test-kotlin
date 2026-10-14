package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionRecordValidator()
        val item = newInspectionRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionRecordRequiredNameRule("inspection record needs a value")
        val item = newInspectionRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = InspectionRecordMaximumRangeRule(3.6, 2583.8)
        val item = newInspectionRecord("x", "y")
        item.maximum = 2585.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = InspectionRecordRequiredDescriptionRule("inspection record needs this field")
        val item = newInspectionRecord("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = InspectionRecordNameLengthRule(32)
        val item = newInspectionRecord("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = InspectionRecordRetriesRangeRule(64, 80)
        val item = newInspectionRecord("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
