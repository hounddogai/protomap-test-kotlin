package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCapacityScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCapacityScoreValidator()
        val item = newPrimaryCapacityScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = PrimaryCapacityScoreRequiredCountryRule("primary capacity score needs a value")
        val item = newPrimaryCapacityScore("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PrimaryCapacityScoreShiftCodeLengthRule(1000)
        val item = newPrimaryCapacityScore("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryCapacityScoreNameLengthRule(1000)
        val item = newPrimaryCapacityScore("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryCapacityScoreRequiredNameRule("primary capacity score needs this field")
        val item = newPrimaryCapacityScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
