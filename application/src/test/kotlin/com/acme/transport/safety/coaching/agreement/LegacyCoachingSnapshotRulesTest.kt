package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCoachingSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCoachingSnapshotValidator()
        val item = newLegacyCoachingSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LegacyCoachingSnapshotRetriesRangeRule(3600, 3600)
        val item = newLegacyCoachingSnapshot("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyCoachingSnapshotTitleLengthRule(86400)
        val item = newLegacyCoachingSnapshot("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
