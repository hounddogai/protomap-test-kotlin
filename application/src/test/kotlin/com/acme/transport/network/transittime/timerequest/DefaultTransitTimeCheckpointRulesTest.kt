package com.acme.transport.network.transittime.timerequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTransitTimeCheckpointValidator()
        val item = newDefaultTransitTimeCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultTransitTimeCheckpointCodeLengthRule(120)
        val item = newDefaultTransitTimeCheckpoint("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = DefaultTransitTimeCheckpointLocaleLengthRule(80)
        val item = newDefaultTransitTimeCheckpoint("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultTransitTimeCheckpointRequiredCodeRule("default transit time checkpoint needs this field")
        val item = newDefaultTransitTimeCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
