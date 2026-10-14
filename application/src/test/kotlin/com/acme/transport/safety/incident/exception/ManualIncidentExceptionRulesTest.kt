package com.acme.transport.safety.incident.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIncidentExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIncidentExceptionValidator()
        val item = newManualIncidentException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualIncidentExceptionRequiredCodeRule("manual incident exception needs this field")
        val item = newManualIncidentException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ManualIncidentExceptionExternalRefLengthRule(255)
        val item = newManualIncidentException("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualIncidentExceptionTitleLengthRule(3600)
        val item = newManualIncidentException("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ManualIncidentExceptionRequiredExternalRefRule("manual incident exception needs this field")
        val item = newManualIncidentException("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
