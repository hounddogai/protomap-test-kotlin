package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualSwapScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualSwapScoreValidator()
        val item = newManualSwapScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualSwapScoreRequiredCodeRule("manual swap score needs an entry")
        val item = newManualSwapScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualSwapScoreTitleLengthRule(32)
        val item = newManualSwapScore("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualSwapScoreRequiredTitleRule("manual swap score needs a value")
        val item = newManualSwapScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ManualSwapScoreChecksumLengthRule(64)
        val item = newManualSwapScore("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
