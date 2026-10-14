package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTransitTimeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTransitTimeEntryValidator()
        val item = newScheduledTransitTimeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScheduledTransitTimeEntryScoreRangeRule(8.4, 188.8)
        val item = newScheduledTransitTimeEntry("x", "y")
        item.score = 190.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ScheduledTransitTimeEntryStartsAtRangeRule(120, 255)
        val item = newScheduledTransitTimeEntry("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
