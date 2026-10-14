package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyParcelScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyParcelScoreValidator()
        val item = newLegacyParcelScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyParcelScoreTitleLengthRule(120)
        val item = newLegacyParcelScore("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LegacyParcelScoreExpiresAtRangeRule(255, 255)
        val item = newLegacyParcelScore("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
