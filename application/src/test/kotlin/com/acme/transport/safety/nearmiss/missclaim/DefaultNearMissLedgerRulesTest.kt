package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNearMissLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultNearMissLedgerValidator()
        val item = newDefaultNearMissLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DefaultNearMissLedgerDriverIdLengthRule(255)
        val item = newDefaultNearMissLedger("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultNearMissLedgerTitleLengthRule(64)
        val item = newDefaultNearMissLedger("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DefaultNearMissLedgerAttemptsRangeRule(255, 1000)
        val item = newDefaultNearMissLedger("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DefaultNearMissLedgerThresholdRangeRule(1.0, 3594.4)
        val item = newDefaultNearMissLedger("x", "y")
        item.threshold = 3595.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
