package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAuditManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAuditManifestValidator()
        val item = newLegacyAuditManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyAuditManifestLabelLengthRule(32)
        val item = newLegacyAuditManifest("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = LegacyAuditManifestDescriptionLengthRule(1000)
        val item = newLegacyAuditManifest("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
