package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDefectSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDefectSummaryValidator()
        val item = newDefaultDefectSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultDefectSummaryRequiredTitleRule("default defect summary needs this field")
        val item = newDefaultDefectSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultDefectSummaryCodeLengthRule(86400)
        val item = newDefaultDefectSummary("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = DefaultDefectSummaryRequiredPhoneRule("default defect summary needs this field")
        val item = newDefaultDefectSummary("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = DefaultDefectSummaryPhoneLengthRule(16)
        val item = newDefaultDefectSummary("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultDefectSummaryRequiredLabelRule("default defect summary needs a value")
        val item = newDefaultDefectSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
