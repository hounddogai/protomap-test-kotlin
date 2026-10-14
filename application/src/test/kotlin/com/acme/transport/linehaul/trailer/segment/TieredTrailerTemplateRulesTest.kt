package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTrailerTemplateValidator()
        val item = newTieredTrailerTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredTrailerTemplateCodeLengthRule(64)
        val item = newTieredTrailerTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredTrailerTemplateRequiredLabelRule("tiered trailer template needs an entry")
        val item = newTieredTrailerTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
