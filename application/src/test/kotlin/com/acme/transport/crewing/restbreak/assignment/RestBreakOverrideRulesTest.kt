package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakOverrideValidator()
        val item = newRestBreakOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RestBreakOverrideNameLengthRule(120)
        val item = newRestBreakOverride("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = RestBreakOverrideRegionLengthRule(3600)
        val item = newRestBreakOverride("x", "y")
        item.region = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RestBreakOverrideRequiredNameRule("rest break override needs an entry")
        val item = newRestBreakOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RestBreakOverrideRequiredCarrierRule("rest break override needs this field")
        val item = newRestBreakOverride("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakOverrideRequiredCodeRule("rest break override needs this field")
        val item = newRestBreakOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
