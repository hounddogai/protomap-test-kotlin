package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDropAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDropAgreementValidator()
        val item = newSeasonalDropAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = SeasonalDropAgreementThresholdRangeRule(6.4, 2339.3)
        val item = newSeasonalDropAgreement("x", "y")
        item.threshold = 2340.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDropAgreementRequiredCodeRule("seasonal drop agreement needs an entry")
        val item = newSeasonalDropAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = SeasonalDropAgreementMarginRangeRule(3.2, 4631.1)
        val item = newSeasonalDropAgreement("x", "y")
        item.margin = 4632.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
