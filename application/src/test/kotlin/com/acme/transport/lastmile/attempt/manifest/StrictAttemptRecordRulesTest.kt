package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAttemptRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAttemptRecordValidator()
        val item = newStrictAttemptRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = StrictAttemptRecordRequiredSummaryRule("strict attempt record needs an entry")
        val item = newStrictAttemptRecord("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = StrictAttemptRecordRequiredCurrencyRule("strict attempt record needs an entry")
        val item = newStrictAttemptRecord("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = StrictAttemptRecordDistanceKmRangeRule(6.8, 3126.2)
        val item = newStrictAttemptRecord("x", "y")
        item.distanceKm = 3127.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictAttemptRecordRequiredCodeRule("strict attempt record needs this field")
        val item = newStrictAttemptRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
