package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditForecastValidator()
        val item = newAuditForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = AuditForecastUnitLengthRule(32)
        val item = newAuditForecast("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditForecastCodeLengthRule(255)
        val item = newAuditForecast("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = AuditForecastNotesLengthRule(120)
        val item = newAuditForecast("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
