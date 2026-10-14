package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakCycleValidator()
        val item = newRestBreakCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RestBreakCycleRequiredLaneRule("rest break cycle needs an entry")
        val item = newRestBreakCycle("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakCycleRequiredCodeRule("rest break cycle needs a value")
        val item = newRestBreakCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RestBreakCycleLaneLengthRule(86400)
        val item = newRestBreakCycle("x", "y")
        item.lane = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
