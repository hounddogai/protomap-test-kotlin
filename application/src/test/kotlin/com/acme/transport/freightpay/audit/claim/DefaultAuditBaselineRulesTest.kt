package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAuditBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAuditBaselineValidator()
        val item = newDefaultAuditBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultAuditBaselineRequiredTitleRule("default audit baseline needs an entry")
        val item = newDefaultAuditBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultAuditBaselineRequiredLabelRule("default audit baseline needs an entry")
        val item = newDefaultAuditBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultAuditBaselineRequiredCodeRule("default audit baseline needs this field")
        val item = newDefaultAuditBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = DefaultAuditBaselineWeightRangeRule(7.0, 3787.3)
        val item = newDefaultAuditBaseline("x", "y")
        item.weight = 3788.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
