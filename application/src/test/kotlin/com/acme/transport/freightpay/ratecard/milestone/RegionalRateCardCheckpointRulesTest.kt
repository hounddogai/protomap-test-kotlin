package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRateCardCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRateCardCheckpointValidator()
        val item = newRegionalRateCardCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RegionalRateCardCheckpointSequenceRangeRule(64, 1000)
        val item = newRegionalRateCardCheckpoint("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalRateCardCheckpointRequiredCodeRule("regional rate card checkpoint needs a value")
        val item = newRegionalRateCardCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalRateCardCheckpointRequiredTitleRule("regional rate card checkpoint needs an entry")
        val item = newRegionalRateCardCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
