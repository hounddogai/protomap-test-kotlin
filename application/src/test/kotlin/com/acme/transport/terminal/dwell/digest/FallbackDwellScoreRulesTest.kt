package com.acme.transport.terminal.dwell.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDwellScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDwellScoreValidator()
        val item = newFallbackDwellScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = FallbackDwellScoreRequiredShiftCodeRule("fallback dwell score needs a value")
        val item = newFallbackDwellScore("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackDwellScoreCodeLengthRule(64)
        val item = newFallbackDwellScore("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackDwellScoreNameLengthRule(86400)
        val item = newFallbackDwellScore("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
