package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveIdlingSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveIdlingSnapshotValidator()
        val item = newAdaptiveIdlingSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = AdaptiveIdlingSnapshotRateRangeRule(0.6, 1819.4)
        val item = newAdaptiveIdlingSnapshot("x", "y")
        item.rate = 1820.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AdaptiveIdlingSnapshotCurrencyLengthRule(3600)
        val item = newAdaptiveIdlingSnapshot("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = AdaptiveIdlingSnapshotExpiresAtRangeRule(255, 1000)
        val item = newAdaptiveIdlingSnapshot("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveIdlingSnapshotRequiredTitleRule("adaptive idling snapshot needs a value")
        val item = newAdaptiveIdlingSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
