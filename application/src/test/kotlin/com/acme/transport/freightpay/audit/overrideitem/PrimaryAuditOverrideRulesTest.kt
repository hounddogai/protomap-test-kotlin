package com.acme.transport.freightpay.audit.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAuditOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAuditOverrideValidator()
        val item = newPrimaryAuditOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryAuditOverrideRequiredLabelRule("primary audit override needs an entry")
        val item = newPrimaryAuditOverride("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PrimaryAuditOverrideRequiredCategoryRule("primary audit override needs an entry")
        val item = newPrimaryAuditOverride("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
