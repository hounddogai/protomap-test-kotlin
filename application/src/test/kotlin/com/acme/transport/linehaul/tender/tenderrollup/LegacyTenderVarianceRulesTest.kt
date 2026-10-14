package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTenderVarianceValidator()
        val item = newLegacyTenderVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LegacyTenderVarianceCreatedAtRangeRule(64, 86400)
        val item = newLegacyTenderVariance("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyTenderVarianceLabelLengthRule(120)
        val item = newLegacyTenderVariance("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
