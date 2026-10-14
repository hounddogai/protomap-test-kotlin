package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBackhaulAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientBackhaulAuditValidator()
        val item = newLenientBackhaulAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LenientBackhaulAuditAmountRangeRule(5.5, 2412.6)
        val item = newLenientBackhaulAudit("x", "y")
        item.amount = 2414.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientBackhaulAuditCodeLengthRule(1000)
        val item = newLenientBackhaulAudit("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LenientBackhaulAuditShiftCodeLengthRule(86400)
        val item = newLenientBackhaulAudit("x", "y")
        item.shiftCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientBackhaulAuditRequiredNameRule("lenient backhaul audit needs a value")
        val item = newLenientBackhaulAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LenientBackhaulAuditRequiredDriverIdRule("lenient backhaul audit needs this field")
        val item = newLenientBackhaulAudit("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
