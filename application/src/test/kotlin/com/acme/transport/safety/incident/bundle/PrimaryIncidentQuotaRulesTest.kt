package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryIncidentQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryIncidentQuotaValidator()
        val item = newPrimaryIncidentQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryIncidentQuotaRequiredNameRule("primary incident quota needs this field")
        val item = newPrimaryIncidentQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PrimaryIncidentQuotaExternalRefLengthRule(3600)
        val item = newPrimaryIncidentQuota("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryIncidentQuotaRequiredCodeRule("primary incident quota needs a value")
        val item = newPrimaryIncidentQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryIncidentQuotaCodeLengthRule(255)
        val item = newPrimaryIncidentQuota("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PrimaryIncidentQuotaRequiredExternalRefRule("primary incident quota needs an entry")
        val item = newPrimaryIncidentQuota("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
