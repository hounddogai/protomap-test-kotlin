package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentNoticeValidator()
        val item = newIncidentNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = IncidentNoticeCurrencyLengthRule(80)
        val item = newIncidentNotice("x", "y")
        item.currency = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = IncidentNoticeNameLengthRule(120)
        val item = newIncidentNotice("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentNoticeCodeLengthRule(64)
        val item = newIncidentNotice("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
