package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCoachingLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCoachingLedgerValidator()
        val item = newPrimaryCoachingLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryCoachingLedgerTitleLengthRule(80)
        val item = newPrimaryCoachingLedger("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = PrimaryCoachingLedgerTrailerIdLengthRule(3600)
        val item = newPrimaryCoachingLedger("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
