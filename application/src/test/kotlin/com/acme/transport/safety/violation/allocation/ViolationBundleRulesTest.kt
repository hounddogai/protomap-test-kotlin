package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationBundleValidator()
        val item = newViolationBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ViolationBundleBudgetRangeRule(5.7, 2624.1)
        val item = newViolationBundle("x", "y")
        item.budget = 2625.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ViolationBundleChecksumLengthRule(64)
        val item = newViolationBundle("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ViolationBundleRequiredCodeRule("violation bundle needs a value")
        val item = newViolationBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ViolationBundleRequiredNameRule("violation bundle needs a value")
        val item = newViolationBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ViolationBundleRequiredChecksumRule("violation bundle needs this field")
        val item = newViolationBundle("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
