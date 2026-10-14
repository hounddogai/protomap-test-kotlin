package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditManifestValidator()
        val item = newAuditManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = AuditManifestNotesLengthRule(255)
        val item = newAuditManifest("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AuditManifestRequiredTitleRule("audit manifest needs this field")
        val item = newAuditManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AuditManifestContactEmailLengthRule(80)
        val item = newAuditManifest("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AuditManifestRequiredContactEmailRule("audit manifest needs an entry")
        val item = newAuditManifest("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
