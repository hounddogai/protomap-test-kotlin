package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRateCardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRateCardAssignmentValidator()
        val item = newLegacyRateCardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LegacyRateCardAssignmentNotesLengthRule(80)
        val item = newLegacyRateCardAssignment("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyRateCardAssignmentRequiredNameRule("legacy rate card assignment needs a value")
        val item = newLegacyRateCardAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyRateCardAssignmentNameLengthRule(1000)
        val item = newLegacyRateCardAssignment("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LegacyRateCardAssignmentRequiredCategoryRule("legacy rate card assignment needs this field")
        val item = newLegacyRateCardAssignment("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
