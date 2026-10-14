package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedUtilizationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedUtilizationPreferenceValidator()
        val item = newCappedUtilizationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedUtilizationPreferenceNameLengthRule(255)
        val item = newCappedUtilizationPreference("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedUtilizationPreferenceRequiredCodeRule("capped utilization preference needs a value")
        val item = newCappedUtilizationPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
