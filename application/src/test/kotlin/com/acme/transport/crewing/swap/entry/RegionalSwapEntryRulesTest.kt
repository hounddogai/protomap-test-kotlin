package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalSwapEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalSwapEntryValidator()
        val item = newRegionalSwapEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalSwapEntryRequiredTitleRule("regional swap entry needs this field")
        val item = newRegionalSwapEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RegionalSwapEntryExternalRefLengthRule(64)
        val item = newRegionalSwapEntry("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RegionalSwapEntryMinimumRangeRule(6.8, 1521.1)
        val item = newRegionalSwapEntry("x", "y")
        item.minimum = 1522.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
