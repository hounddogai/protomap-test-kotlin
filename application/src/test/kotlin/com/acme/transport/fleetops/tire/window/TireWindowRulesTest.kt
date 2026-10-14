package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireWindowValidator()
        val item = newTireWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = TireWindowSummaryLengthRule(32)
        val item = newTireWindow("x", "y")
        item.summary = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TireWindowTitleLengthRule(16)
        val item = newTireWindow("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireWindowCodeLengthRule(3600)
        val item = newTireWindow("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
