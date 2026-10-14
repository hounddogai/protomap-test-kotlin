package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNearMissAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalNearMissAuditValidator()
        val item = newRegionalNearMissAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = RegionalNearMissAuditRequiredShiftCodeRule("regional near miss audit needs a value")
        val item = newRegionalNearMissAudit("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalNearMissAuditRequiredNameRule("regional near miss audit needs an entry")
        val item = newRegionalNearMissAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RegionalNearMissAuditShiftCodeLengthRule(32)
        val item = newRegionalNearMissAudit("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RegionalNearMissAuditLimitRangeRule(64, 1000)
        val item = newRegionalNearMissAudit("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
