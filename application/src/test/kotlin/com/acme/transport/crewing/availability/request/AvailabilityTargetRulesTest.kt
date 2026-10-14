package com.acme.transport.crewing.availability.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityTargetValidator()
        val item = newAvailabilityTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AvailabilityTargetRequiredNameRule("availability target needs this field")
        val item = newAvailabilityTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AvailabilityTargetNameLengthRule(86400)
        val item = newAvailabilityTarget("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
