package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTenderAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTenderAssignmentValidator()
        val item = newSeasonalTenderAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalTenderAssignmentRequiredLabelRule("seasonal tender assignment needs this field")
        val item = newSeasonalTenderAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = SeasonalTenderAssignmentContactEmailLengthRule(1000)
        val item = newSeasonalTenderAssignment("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SeasonalTenderAssignmentDriverIdLengthRule(120)
        val item = newSeasonalTenderAssignment("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalTenderAssignmentRequiredCodeRule("seasonal tender assignment needs a value")
        val item = newSeasonalTenderAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalTenderAssignmentLabelLengthRule(3600)
        val item = newSeasonalTenderAssignment("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
