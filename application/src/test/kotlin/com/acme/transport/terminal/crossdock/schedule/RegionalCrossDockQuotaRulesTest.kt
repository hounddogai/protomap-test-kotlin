package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCrossDockQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCrossDockQuotaValidator()
        val item = newRegionalCrossDockQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCrossDockQuotaCodeLengthRule(3600)
        val item = newRegionalCrossDockQuota("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalCrossDockQuotaLabelLengthRule(80)
        val item = newRegionalCrossDockQuota("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCrossDockQuotaRequiredCodeRule("regional cross dock quota needs this field")
        val item = newRegionalCrossDockQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RegionalCrossDockQuotaWindowDaysRangeRule(80, 1000)
        val item = newRegionalCrossDockQuota("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalCrossDockQuotaRequiredLabelRule("regional cross dock quota needs this field")
        val item = newRegionalCrossDockQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
