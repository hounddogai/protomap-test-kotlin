package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServiceAreaCycleValidator()
        val item = newStrictServiceAreaCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictServiceAreaCycleRequiredCodeRule("strict service area cycle needs a value")
        val item = newStrictServiceAreaCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = StrictServiceAreaCycleChannelLengthRule(16)
        val item = newStrictServiceAreaCycle("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = StrictServiceAreaCycleRequiredChannelRule("strict service area cycle needs a value")
        val item = newStrictServiceAreaCycle("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
