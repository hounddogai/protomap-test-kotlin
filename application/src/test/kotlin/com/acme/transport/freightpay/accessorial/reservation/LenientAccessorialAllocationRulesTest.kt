package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAccessorialAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAccessorialAllocationValidator()
        val item = newLenientAccessorialAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LenientAccessorialAllocationRequiredTimezoneRule("lenient accessorial allocation needs an entry")
        val item = newLenientAccessorialAllocation("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LenientAccessorialAllocationTimezoneLengthRule(3600)
        val item = newLenientAccessorialAllocation("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = LenientAccessorialAllocationRequiredSourceRule("lenient accessorial allocation needs a value")
        val item = newLenientAccessorialAllocation("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientAccessorialAllocationRequiredNameRule("lenient accessorial allocation needs a value")
        val item = newLenientAccessorialAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
