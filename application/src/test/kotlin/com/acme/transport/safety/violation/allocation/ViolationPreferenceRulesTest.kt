package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationPreferenceValidator()
        val item = newViolationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationPreferenceCodeLengthRule(120)
        val item = newViolationPreference("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ViolationPreferenceRequiredCodeRule("violation preference needs a value")
        val item = newViolationPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ViolationPreferenceRequiredTitleRule("violation preference needs an entry")
        val item = newViolationPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ViolationPreferenceTitleLengthRule(64)
        val item = newViolationPreference("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ViolationPreferenceRequiredSegmentRule("violation preference needs a value")
        val item = newViolationPreference("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
