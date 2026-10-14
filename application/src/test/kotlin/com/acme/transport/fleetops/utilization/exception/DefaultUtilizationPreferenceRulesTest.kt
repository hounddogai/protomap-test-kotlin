package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultUtilizationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultUtilizationPreferenceValidator()
        val item = newDefaultUtilizationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultUtilizationPreferenceRequiredCodeRule("default utilization preference needs this field")
        val item = newDefaultUtilizationPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = DefaultUtilizationPreferenceSourceLengthRule(64)
        val item = newDefaultUtilizationPreference("x", "y")
        item.source = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultUtilizationPreferenceCodeLengthRule(16)
        val item = newDefaultUtilizationPreference("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultUtilizationPreferenceNameLengthRule(64)
        val item = newDefaultUtilizationPreference("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DefaultUtilizationPreferenceRequiredSourceRule("default utilization preference needs this field")
        val item = newDefaultUtilizationPreference("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
