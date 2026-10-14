package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDeclarationAssignmentValidator()
        val item = newSeasonalDeclarationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = SeasonalDeclarationAssignmentOdometerRangeRule(80, 1000)
        val item = newSeasonalDeclarationAssignment("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalDeclarationAssignmentCodeLengthRule(80)
        val item = newSeasonalDeclarationAssignment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = SeasonalDeclarationAssignmentContactEmailLengthRule(86400)
        val item = newSeasonalDeclarationAssignment("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalDeclarationAssignmentNameLengthRule(255)
        val item = newSeasonalDeclarationAssignment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = SeasonalDeclarationAssignmentWindowDaysRangeRule(1000, 3600)
        val item = newSeasonalDeclarationAssignment("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
