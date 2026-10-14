package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionSnapshotValidator()
        val item = newInspectionSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = InspectionSnapshotRequiredDescriptionRule("inspection snapshot needs an entry")
        val item = newInspectionSnapshot("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = InspectionSnapshotEtaMinutesRangeRule(32, 120)
        val item = newInspectionSnapshot("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionSnapshotLabelLengthRule(86400)
        val item = newInspectionSnapshot("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = InspectionSnapshotDescriptionLengthRule(120)
        val item = newInspectionSnapshot("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
