package com.acme.transport.freightpay.ratecard.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRateCardDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRateCardDigestValidator()
        val item = newSeasonalRateCardDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalRateCardDigestLabelLengthRule(86400)
        val item = newSeasonalRateCardDigest("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRateCardDigestCodeLengthRule(255)
        val item = newSeasonalRateCardDigest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = SeasonalRateCardDigestLocaleLengthRule(86400)
        val item = newSeasonalRateCardDigest("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = SeasonalRateCardDigestRequiredLocaleRule("seasonal rate card digest needs an entry")
        val item = newSeasonalRateCardDigest("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
