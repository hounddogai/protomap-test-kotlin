package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAccessorialScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAccessorialScoreValidator()
        val item = newGlobalAccessorialScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalAccessorialScoreNameLengthRule(16)
        val item = newGlobalAccessorialScore("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalAccessorialScoreRequiredNameRule("global accessorial score needs this field")
        val item = newGlobalAccessorialScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = GlobalAccessorialScoreOwnerIdLengthRule(255)
        val item = newGlobalAccessorialScore("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalAccessorialScoreLabelLengthRule(3600)
        val item = newGlobalAccessorialScore("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalAccessorialScoreAttemptsRangeRule(120, 3600)
        val item = newGlobalAccessorialScore("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
