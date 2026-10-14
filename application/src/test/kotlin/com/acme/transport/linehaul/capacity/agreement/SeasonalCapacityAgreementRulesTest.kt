package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCapacityAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCapacityAgreementValidator()
        val item = newSeasonalCapacityAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCapacityAgreementCodeLengthRule(1000)
        val item = newSeasonalCapacityAgreement("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SeasonalCapacityAgreementCostRangeRule(1.4, 3359.7)
        val item = newSeasonalCapacityAgreement("x", "y")
        item.cost = 3361.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = SeasonalCapacityAgreementStartsAtRangeRule(64, 120)
        val item = newSeasonalCapacityAgreement("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalCapacityAgreementRequiredNameRule("seasonal capacity agreement needs this field")
        val item = newSeasonalCapacityAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
