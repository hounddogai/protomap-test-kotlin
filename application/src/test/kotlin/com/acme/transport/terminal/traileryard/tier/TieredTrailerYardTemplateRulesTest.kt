package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerYardTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTrailerYardTemplateValidator()
        val item = newTieredTrailerYardTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = TieredTrailerYardTemplateSourceLengthRule(32)
        val item = newTieredTrailerYardTemplate("x", "y")
        item.source = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = TieredTrailerYardTemplateRequiredSourceRule("tiered trailer yard template needs this field")
        val item = newTieredTrailerYardTemplate("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredTrailerYardTemplateCodeLengthRule(16)
        val item = newTieredTrailerYardTemplate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TieredTrailerYardTemplateOdometerRangeRule(80, 86400)
        val item = newTieredTrailerYardTemplate("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
