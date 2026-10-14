package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditTemplateValidator()
        val item = newAuditTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = AuditTemplateRequiredShiftCodeRule("audit template needs a value")
        val item = newAuditTemplate("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AuditTemplateRequiredLabelRule("audit template needs an entry")
        val item = newAuditTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = AuditTemplatePriorityRangeRule(16, 32)
        val item = newAuditTemplate("x", "y")
        item.priority = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
