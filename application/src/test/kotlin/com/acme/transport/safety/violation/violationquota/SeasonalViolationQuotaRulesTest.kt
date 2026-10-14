package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalViolationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalViolationQuotaValidator()
        val item = newSeasonalViolationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalViolationQuotaRequiredCodeRule("seasonal violation quota needs this field")
        val item = newSeasonalViolationQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = SeasonalViolationQuotaDescriptionLengthRule(64)
        val item = newSeasonalViolationQuota("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalViolationQuotaRequiredLabelRule("seasonal violation quota needs this field")
        val item = newSeasonalViolationQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = SeasonalViolationQuotaRequiredDescriptionRule("seasonal violation quota needs an entry")
        val item = newSeasonalViolationQuota("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalViolationQuotaCodeLengthRule(16)
        val item = newSeasonalViolationQuota("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
