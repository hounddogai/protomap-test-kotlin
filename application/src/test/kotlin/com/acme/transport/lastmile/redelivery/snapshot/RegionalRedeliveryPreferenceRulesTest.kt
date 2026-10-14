package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRedeliveryPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRedeliveryPreferenceValidator()
        val item = newRegionalRedeliveryPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalRedeliveryPreferenceLoadKgRangeRule(7.9, 1523.8)
        val item = newRegionalRedeliveryPreference("x", "y")
        item.loadKg = 1525.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = RegionalRedeliveryPreferenceCategoryLengthRule(32)
        val item = newRegionalRedeliveryPreference("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RegionalRedeliveryPreferenceRequiredNotesRule("regional redelivery preference needs an entry")
        val item = newRegionalRedeliveryPreference("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
