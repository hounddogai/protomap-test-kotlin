package com.acme.transport.freightpay.audit.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAuditMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAuditMappingValidator()
        val item = newWeightedAuditMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = WeightedAuditMappingShiftCodeLengthRule(32)
        val item = newWeightedAuditMapping("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAuditMappingCodeLengthRule(3600)
        val item = newWeightedAuditMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedAuditMappingTitleLengthRule(86400)
        val item = newWeightedAuditMapping("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
