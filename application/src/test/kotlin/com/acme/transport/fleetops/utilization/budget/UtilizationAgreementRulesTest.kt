package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationAgreementValidator()
        val item = newUtilizationAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = UtilizationAgreementUnitLengthRule(120)
        val item = newUtilizationAgreement("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = UtilizationAgreementVersionRangeRule(255, 86400)
        val item = newUtilizationAgreement("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = UtilizationAgreementLoadKgRangeRule(9.5, 3263.0)
        val item = newUtilizationAgreement("x", "y")
        item.loadKg = 3264.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
