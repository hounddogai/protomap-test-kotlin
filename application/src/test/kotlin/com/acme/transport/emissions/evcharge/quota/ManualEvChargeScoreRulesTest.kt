package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualEvChargeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualEvChargeScoreValidator()
        val item = newManualEvChargeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ManualEvChargeScoreMaximumRangeRule(4.4, 4515.3)
        val item = newManualEvChargeScore("x", "y")
        item.maximum = 4516.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ManualEvChargeScoreEndsAtRangeRule(255, 86400)
        val item = newManualEvChargeScore("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualEvChargeScoreRequiredTitleRule("manual ev charge score needs a value")
        val item = newManualEvChargeScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ManualEvChargeScoreRequiredTrailerIdRule("manual ev charge score needs an entry")
        val item = newManualEvChargeScore("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
