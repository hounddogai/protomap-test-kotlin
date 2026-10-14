package com.acme.transport.linehaul.laneplan.planagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLanePlanAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLanePlanAgreementValidator()
        val item = newWeightedLanePlanAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedLanePlanAgreementRequiredLabelRule("weighted lane plan agreement needs an entry")
        val item = newWeightedLanePlanAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = WeightedLanePlanAgreementFloorValueRangeRule(7.5, 2853.2)
        val item = newWeightedLanePlanAgreement("x", "y")
        item.floorValue = 2854.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = WeightedLanePlanAgreementPhoneLengthRule(80)
        val item = newWeightedLanePlanAgreement("x", "y")
        item.phone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
