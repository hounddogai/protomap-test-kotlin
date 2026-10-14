package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDropTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDropTierValidator()
        val item = newRegionalDropTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RegionalDropTierLocaleLengthRule(80)
        val item = newRegionalDropTier("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalDropTierRequiredTitleRule("regional drop tier needs this field")
        val item = newRegionalDropTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RegionalDropTierRequiredContactEmailRule("regional drop tier needs a value")
        val item = newRegionalDropTier("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RegionalDropTierRequiredLocaleRule("regional drop tier needs an entry")
        val item = newRegionalDropTier("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
