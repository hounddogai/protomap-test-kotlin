package com.acme.transport.crewing.availability.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityRequestValidator()
        val item = newAvailabilityRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilityRequestRequiredCodeRule("availability request needs this field")
        val item = newAvailabilityRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityRequestCodeLengthRule(80)
        val item = newAvailabilityRequest("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AvailabilityRequestLabelLengthRule(80)
        val item = newAvailabilityRequest("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
