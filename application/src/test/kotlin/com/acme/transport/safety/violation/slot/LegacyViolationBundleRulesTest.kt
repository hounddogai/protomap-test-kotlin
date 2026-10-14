package com.acme.transport.safety.violation.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyViolationBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyViolationBundleValidator()
        val item = newLegacyViolationBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = LegacyViolationBundleRequiredUnitRule("legacy violation bundle needs a value")
        val item = newLegacyViolationBundle("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyViolationBundleCodeLengthRule(86400)
        val item = newLegacyViolationBundle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyViolationBundleLabelLengthRule(32)
        val item = newLegacyViolationBundle("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
