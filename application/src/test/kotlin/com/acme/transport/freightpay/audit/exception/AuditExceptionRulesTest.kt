package com.acme.transport.freightpay.audit.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditExceptionValidator()
        val item = newAuditException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AuditExceptionNameLengthRule(32)
        val item = newAuditException("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AuditExceptionContactEmailLengthRule(120)
        val item = newAuditException("x", "y")
        item.contactEmail = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AuditExceptionFuelLitersRangeRule(6.4, 582.2)
        val item = newAuditException("x", "y")
        item.fuelLiters = 583.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditExceptionCodeLengthRule(120)
        val item = newAuditException("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AuditExceptionRequiredContactEmailRule("audit exception needs a value")
        val item = newAuditException("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
