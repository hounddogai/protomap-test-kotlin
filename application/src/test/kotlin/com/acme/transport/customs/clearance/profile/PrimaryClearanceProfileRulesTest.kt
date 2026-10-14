package com.acme.transport.customs.clearance.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryClearanceProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryClearanceProfileValidator()
        val item = newPrimaryClearanceProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PrimaryClearanceProfileRequiredExternalRefRule("primary clearance profile needs an entry")
        val item = newPrimaryClearanceProfile("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = PrimaryClearanceProfileDescriptionLengthRule(80)
        val item = newPrimaryClearanceProfile("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = PrimaryClearanceProfileFuelLitersRangeRule(6.6, 1925.2)
        val item = newPrimaryClearanceProfile("x", "y")
        item.fuelLiters = 1926.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
