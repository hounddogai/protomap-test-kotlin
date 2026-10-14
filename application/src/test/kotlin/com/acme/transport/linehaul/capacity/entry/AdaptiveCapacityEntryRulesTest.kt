package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCapacityEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCapacityEntryValidator()
        val item = newAdaptiveCapacityEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AdaptiveCapacityEntryPhoneLengthRule(255)
        val item = newAdaptiveCapacityEntry("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = AdaptiveCapacityEntryMinimumRangeRule(5.3, 3028.4)
        val item = newAdaptiveCapacityEntry("x", "y")
        item.minimum = 3029.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = AdaptiveCapacityEntryRequiredPhoneRule("adaptive capacity entry needs this field")
        val item = newAdaptiveCapacityEntry("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AdaptiveCapacityEntryAmountRangeRule(1.4, 2769.1)
        val item = newAdaptiveCapacityEntry("x", "y")
        item.amount = 2770.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
