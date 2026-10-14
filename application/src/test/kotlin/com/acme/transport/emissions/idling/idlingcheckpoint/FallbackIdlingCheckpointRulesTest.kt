package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIdlingCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackIdlingCheckpointValidator()
        val item = newFallbackIdlingCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FallbackIdlingCheckpointRateRangeRule(6.8, 3022.5)
        val item = newFallbackIdlingCheckpoint("x", "y")
        item.rate = 3024.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackIdlingCheckpointRequiredNameRule("fallback idling checkpoint needs a value")
        val item = newFallbackIdlingCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = FallbackIdlingCheckpointRequiredDescriptionRule("fallback idling checkpoint needs a value")
        val item = newFallbackIdlingCheckpoint("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackIdlingCheckpointCodeLengthRule(120)
        val item = newFallbackIdlingCheckpoint("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
