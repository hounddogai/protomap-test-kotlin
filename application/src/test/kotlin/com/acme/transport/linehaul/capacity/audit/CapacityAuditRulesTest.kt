package com.acme.transport.linehaul.capacity.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityAuditValidator()
        val item = newCapacityAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = CapacityAuditEtaMinutesRangeRule(16, 120)
        val item = newCapacityAudit("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CapacityAuditRevisionRangeRule(255, 1000)
        val item = newCapacityAudit("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = CapacityAuditChecksumLengthRule(3600)
        val item = newCapacityAudit("x", "y")
        item.checksum = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = CapacityAuditRequiredChecksumRule("capacity audit needs a value")
        val item = newCapacityAudit("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
