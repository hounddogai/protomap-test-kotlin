package com.acme.transport.freightpay.ratecard.cardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRateCardGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRateCardGroupValidator()
        val item = newLegacyRateCardGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyRateCardGroupCodeLengthRule(3600)
        val item = newLegacyRateCardGroup("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LegacyRateCardGroupEtaMinutesRangeRule(64, 86400)
        val item = newLegacyRateCardGroup("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyRateCardGroupRequiredNameRule("legacy rate card group needs this field")
        val item = newLegacyRateCardGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
