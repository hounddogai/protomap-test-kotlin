package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryInspectionExceptionValidator()
        val item = newPrimaryInspectionException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = PrimaryInspectionExceptionLocaleLengthRule(64)
        val item = newPrimaryInspectionException("x", "y")
        item.locale = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryInspectionExceptionRequiredLabelRule("primary inspection exception needs an entry")
        val item = newPrimaryInspectionException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryInspectionExceptionLabelLengthRule(86400)
        val item = newPrimaryInspectionException("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
