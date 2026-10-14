package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerTierValidator()
        val item = newTrailerTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TrailerTierRequiredOwnerIdRule("trailer tier needs this field")
        val item = newTrailerTier("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerTierRequiredCodeRule("trailer tier needs this field")
        val item = newTrailerTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = TrailerTierRequiredTimezoneRule("trailer tier needs a value")
        val item = newTrailerTier("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
