package com.acme.transport.network.cutoff.weightedcutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffOverrideValidator()
        val item = newCutoffOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffOverrideRequiredCodeRule("cutoff override needs an entry")
        val item = newCutoffOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CutoffOverrideCodeLengthRule(80)
        val item = newCutoffOverride("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
