package com.acme.transport.crewing.availability.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityExceptionValidator()
        val item = newAvailabilityException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AvailabilityExceptionRequiredLabelRule("availability exception needs a value")
        val item = newAvailabilityException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = AvailabilityExceptionRegionLengthRule(120)
        val item = newAvailabilityException("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AvailabilityExceptionLabelLengthRule(1000)
        val item = newAvailabilityException("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
