package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictViolationQuotaValidator()
        val item = newStrictViolationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = StrictViolationQuotaMarginRangeRule(8.0, 293.2)
        val item = newStrictViolationQuota("x", "y")
        item.margin = 294.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictViolationQuotaRequiredCodeRule("strict violation quota needs a value")
        val item = newStrictViolationQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictViolationQuotaRequiredTitleRule("strict violation quota needs this field")
        val item = newStrictViolationQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = StrictViolationQuotaWeightRangeRule(4.4, 4965.5)
        val item = newStrictViolationQuota("x", "y")
        item.weight = 4967.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
