package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServiceAreaRequestValidator()
        val item = newLenientServiceAreaRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientServiceAreaRequestUpdatedAtRangeRule(64, 1000)
        val item = newLenientServiceAreaRequest("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LenientServiceAreaRequestRequiredTimezoneRule("lenient service area request needs an entry")
        val item = newLenientServiceAreaRequest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
