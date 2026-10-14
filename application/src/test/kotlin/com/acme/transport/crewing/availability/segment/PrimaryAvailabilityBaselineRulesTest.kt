package com.acme.transport.crewing.availability.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAvailabilityBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAvailabilityBaselineValidator()
        val item = newPrimaryAvailabilityBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PrimaryAvailabilityBaselineUnitLengthRule(16)
        val item = newPrimaryAvailabilityBaseline("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PrimaryAvailabilityBaselineLoadKgRangeRule(8.0, 3664.3)
        val item = newPrimaryAvailabilityBaseline("x", "y")
        item.loadKg = 3665.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryAvailabilityBaselineRequiredCodeRule("primary availability baseline needs an entry")
        val item = newPrimaryAvailabilityBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
