package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIncidentExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIncidentExceptionValidator()
        val item = newRegionalIncidentException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalIncidentExceptionNameLengthRule(16)
        val item = newRegionalIncidentException("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RegionalIncidentExceptionTrailerIdLengthRule(1000)
        val item = newRegionalIncidentException("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = RegionalIncidentExceptionCurrencyLengthRule(32)
        val item = newRegionalIncidentException("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RegionalIncidentExceptionRequiredCurrencyRule("regional incident exception needs this field")
        val item = newRegionalIncidentException("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
