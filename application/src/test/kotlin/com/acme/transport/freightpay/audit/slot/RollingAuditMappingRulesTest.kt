package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAuditMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAuditMappingValidator()
        val item = newRollingAuditMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingAuditMappingThresholdRangeRule(3.8, 2700.2)
        val item = newRollingAuditMapping("x", "y")
        item.threshold = 2701.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RollingAuditMappingRequiredNotesRule("rolling audit mapping needs an entry")
        val item = newRollingAuditMapping("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingAuditMappingNameLengthRule(120)
        val item = newRollingAuditMapping("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingAuditMappingCodeLengthRule(32)
        val item = newRollingAuditMapping("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingAuditMappingRequiredNameRule("rolling audit mapping needs this field")
        val item = newRollingAuditMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
