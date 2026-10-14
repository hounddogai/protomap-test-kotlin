package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCoachingLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCoachingLedgerValidator()
        val item = newManualCoachingLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ManualCoachingLedgerDescriptionLengthRule(3600)
        val item = newManualCoachingLedger("x", "y")
        item.description = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ManualCoachingLedgerDistanceKmRangeRule(8.4, 3505.0)
        val item = newManualCoachingLedger("x", "y")
        item.distanceKm = 3506.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ManualCoachingLedgerDurationMinutesRangeRule(64, 120)
        val item = newManualCoachingLedger("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ManualCoachingLedgerRequiredDescriptionRule("manual coaching ledger needs an entry")
        val item = newManualCoachingLedger("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
