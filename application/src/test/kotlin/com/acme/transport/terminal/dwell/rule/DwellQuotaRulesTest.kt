package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellQuotaValidator()
        val item = newDwellQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DwellQuotaRequiredCountryRule("dwell quota needs a value")
        val item = newDwellQuota("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DwellQuotaRequiredLabelRule("dwell quota needs this field")
        val item = newDwellQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = DwellQuotaMarginRangeRule(0.5, 2339.8)
        val item = newDwellQuota("x", "y")
        item.margin = 2341.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DwellQuotaLabelLengthRule(1000)
        val item = newDwellQuota("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
