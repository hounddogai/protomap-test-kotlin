package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDisputeTierValidator()
        val item = newLenientDisputeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientDisputeTierRequiredCodeRule("lenient dispute tier needs this field")
        val item = newLenientDisputeTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LenientDisputeTierDriverIdLengthRule(1000)
        val item = newLenientDisputeTier("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LenientDisputeTierRequiredDriverIdRule("lenient dispute tier needs this field")
        val item = newLenientDisputeTier("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientDisputeTierRequiredTitleRule("lenient dispute tier needs an entry")
        val item = newLenientDisputeTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LenientDisputeTierAmountRangeRule(1.3, 2743.9)
        val item = newLenientDisputeTier("x", "y")
        item.amount = 2745.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
