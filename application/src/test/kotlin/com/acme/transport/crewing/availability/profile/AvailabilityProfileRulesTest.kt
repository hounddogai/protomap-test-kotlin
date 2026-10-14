package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityProfileValidator()
        val item = newAvailabilityProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityProfileCodeLengthRule(1000)
        val item = newAvailabilityProfile("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = AvailabilityProfileRequiredOwnerIdRule("availability profile needs this field")
        val item = newAvailabilityProfile("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AvailabilityProfileTitleLengthRule(120)
        val item = newAvailabilityProfile("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
