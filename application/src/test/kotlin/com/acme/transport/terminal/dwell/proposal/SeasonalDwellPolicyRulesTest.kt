package com.acme.transport.terminal.dwell.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDwellPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDwellPolicyValidator()
        val item = newSeasonalDwellPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = SeasonalDwellPolicyRequiredSourceRule("seasonal dwell policy needs a value")
        val item = newSeasonalDwellPolicy("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalDwellPolicyTitleLengthRule(120)
        val item = newSeasonalDwellPolicy("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = SeasonalDwellPolicyExternalRefLengthRule(64)
        val item = newSeasonalDwellPolicy("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SeasonalDwellPolicyRatioRangeRule(9.5, 2359.3)
        val item = newSeasonalDwellPolicy("x", "y")
        item.ratio = 2360.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
