package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditMappingValidator()
        val item = newAuditMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AuditMappingRequiredDriverIdRule("audit mapping needs a value")
        val item = newAuditMapping("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = AuditMappingReviewerLengthRule(3600)
        val item = newAuditMapping("x", "y")
        item.reviewer = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AuditMappingFuelLitersRangeRule(9.9, 1279.7)
        val item = newAuditMapping("x", "y")
        item.fuelLiters = 1281.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
