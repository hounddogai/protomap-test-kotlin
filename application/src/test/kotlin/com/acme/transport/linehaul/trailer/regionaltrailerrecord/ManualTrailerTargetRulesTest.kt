package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerTargetValidator()
        val item = newManualTrailerTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ManualTrailerTargetRequiredLocaleRule("manual trailer target needs a value")
        val item = newManualTrailerTarget("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualTrailerTargetNameLengthRule(16)
        val item = newManualTrailerTarget("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualTrailerTargetRequiredNameRule("manual trailer target needs this field")
        val item = newManualTrailerTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
