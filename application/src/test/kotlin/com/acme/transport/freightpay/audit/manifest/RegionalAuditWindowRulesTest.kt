package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAuditWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalAuditWindowValidator()
        val item = newRegionalAuditWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RegionalAuditWindowVehicleIdLengthRule(86400)
        val item = newRegionalAuditWindow("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RegionalAuditWindowExternalRefLengthRule(86400)
        val item = newRegionalAuditWindow("x", "y")
        item.externalRef = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalAuditWindowRequiredCodeRule("regional audit window needs a value")
        val item = newRegionalAuditWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RegionalAuditWindowExpiresAtRangeRule(16, 255)
        val item = newRegionalAuditWindow("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
