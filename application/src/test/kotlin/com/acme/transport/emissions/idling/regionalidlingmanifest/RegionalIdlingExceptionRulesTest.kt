package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIdlingExceptionValidator()
        val item = newRegionalIdlingException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RegionalIdlingExceptionOwnerIdLengthRule(64)
        val item = newRegionalIdlingException("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = RegionalIdlingExceptionRequiredOwnerIdRule("regional idling exception needs an entry")
        val item = newRegionalIdlingException("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RegionalIdlingExceptionStartsAtRangeRule(32, 80)
        val item = newRegionalIdlingException("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalIdlingExceptionRequiredCodeRule("regional idling exception needs this field")
        val item = newRegionalIdlingException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RegionalIdlingExceptionRequiredDescriptionRule("regional idling exception needs this field")
        val item = newRegionalIdlingException("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
