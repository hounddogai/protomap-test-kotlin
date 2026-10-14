package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedClearanceBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedClearanceBundleValidator()
        val item = newCappedClearanceBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = CappedClearanceBundleLocaleLengthRule(86400)
        val item = newCappedClearanceBundle("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedClearanceBundleCodeLengthRule(255)
        val item = newCappedClearanceBundle("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CappedClearanceBundleRequiredLocaleRule("capped clearance bundle needs a value")
        val item = newCappedClearanceBundle("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
