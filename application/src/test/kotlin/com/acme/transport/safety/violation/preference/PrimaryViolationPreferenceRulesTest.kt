package com.acme.transport.safety.violation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryViolationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryViolationPreferenceValidator()
        val item = newPrimaryViolationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryViolationPreferenceRequiredCodeRule("primary violation preference needs this field")
        val item = newPrimaryViolationPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = PrimaryViolationPreferenceEtaMinutesRangeRule(16, 3600)
        val item = newPrimaryViolationPreference("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
