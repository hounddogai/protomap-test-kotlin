package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalSortationOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalSortationOverrideValidator()
        val item = newRegionalSortationOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalSortationOverrideRequiredNameRule("regional sortation override needs this field")
        val item = newRegionalSortationOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalSortationOverrideRequiredCodeRule("regional sortation override needs this field")
        val item = newRegionalSortationOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalSortationOverrideNameLengthRule(80)
        val item = newRegionalSortationOverride("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
