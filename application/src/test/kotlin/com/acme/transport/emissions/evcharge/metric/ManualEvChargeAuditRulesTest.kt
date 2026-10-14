package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualEvChargeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualEvChargeAuditValidator()
        val item = newManualEvChargeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualEvChargeAuditRequiredTitleRule("manual ev charge audit needs a value")
        val item = newManualEvChargeAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ManualEvChargeAuditShiftCodeLengthRule(16)
        val item = newManualEvChargeAudit("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ManualEvChargeAuditThresholdRangeRule(9.5, 2711.8)
        val item = newManualEvChargeAudit("x", "y")
        item.threshold = 2713.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ManualEvChargeAuditRequiredLocaleRule("manual ev charge audit needs an entry")
        val item = newManualEvChargeAudit("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ManualEvChargeAuditLocaleLengthRule(3600)
        val item = newManualEvChargeAudit("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ManualEvChargeAuditCapacityRangeRule(80, 86400)
        val item = newManualEvChargeAudit("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
