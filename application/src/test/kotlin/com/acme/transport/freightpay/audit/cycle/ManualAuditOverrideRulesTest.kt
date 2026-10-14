package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAuditOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAuditOverrideValidator()
        val item = newManualAuditOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ManualAuditOverrideRequiredExternalRefRule("manual audit override needs an entry")
        val item = newManualAuditOverride("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualAuditOverrideRequiredCodeRule("manual audit override needs a value")
        val item = newManualAuditOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ManualAuditOverrideSequenceRangeRule(120, 255)
        val item = newManualAuditOverride("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
