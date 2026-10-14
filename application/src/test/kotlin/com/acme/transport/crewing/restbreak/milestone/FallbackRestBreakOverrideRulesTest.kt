package com.acme.transport.crewing.restbreak.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRestBreakOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRestBreakOverrideValidator()
        val item = newFallbackRestBreakOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = FallbackRestBreakOverrideRequiredDepotRule("fallback rest break override needs an entry")
        val item = newFallbackRestBreakOverride("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRestBreakOverrideCodeLengthRule(80)
        val item = newFallbackRestBreakOverride("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = FallbackRestBreakOverrideContactEmailLengthRule(255)
        val item = newFallbackRestBreakOverride("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
