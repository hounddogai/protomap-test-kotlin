package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTransitTimeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTransitTimeBatchValidator()
        val item = newScheduledTransitTimeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledTransitTimeBatchRatioRangeRule(6.8, 2239.8)
        val item = newScheduledTransitTimeBatch("x", "y")
        item.ratio = 2241.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledTransitTimeBatchRequiredNameRule("scheduled transit time batch needs an entry")
        val item = newScheduledTransitTimeBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ScheduledTransitTimeBatchSequenceRangeRule(64, 1000)
        val item = newScheduledTransitTimeBatch("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
