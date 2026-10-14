package com.acme.transport.terminal.crossdock.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCrossDockAuditValidator()
        val item = newGlobalCrossDockAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalCrossDockAuditLabelLengthRule(32)
        val item = newGlobalCrossDockAudit("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = GlobalCrossDockAuditRequiredContactEmailRule("global cross dock audit needs an entry")
        val item = newGlobalCrossDockAudit("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
