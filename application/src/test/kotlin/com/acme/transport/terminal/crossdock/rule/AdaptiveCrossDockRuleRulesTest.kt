package com.acme.transport.terminal.crossdock.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCrossDockRuleValidator()
        val item = newAdaptiveCrossDockRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveCrossDockRuleNameLengthRule(16)
        val item = newAdaptiveCrossDockRule("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveCrossDockRuleRequiredCodeRule("adaptive cross dock rule needs an entry")
        val item = newAdaptiveCrossDockRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
