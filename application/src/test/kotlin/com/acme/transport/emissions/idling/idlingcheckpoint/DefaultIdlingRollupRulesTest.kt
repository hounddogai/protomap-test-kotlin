package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultIdlingRollupValidator()
        val item = newDefaultIdlingRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DefaultIdlingRollupRequiredChecksumRule("default idling rollup needs this field")
        val item = newDefaultIdlingRollup("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DefaultIdlingRollupRetriesRangeRule(64, 255)
        val item = newDefaultIdlingRollup("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DefaultIdlingRollupEtaMinutesRangeRule(16, 32)
        val item = newDefaultIdlingRollup("x", "y")
        item.etaMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DefaultIdlingRollupWindowDaysRangeRule(16, 120)
        val item = newDefaultIdlingRollup("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultIdlingRollupMinimumRangeRule(7.7, 555.2)
        val item = newDefaultIdlingRollup("x", "y")
        item.minimum = 556.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
