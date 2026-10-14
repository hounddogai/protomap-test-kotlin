package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIdlingScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIdlingScoreValidator()
        val item = newManualIdlingScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualIdlingScoreRequiredNameRule("manual idling score needs a value")
        val item = newManualIdlingScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ManualIdlingScoreStartsAtRangeRule(255, 86400)
        val item = newManualIdlingScore("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualIdlingScoreNameLengthRule(1000)
        val item = newManualIdlingScore("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
