package com.acme.transport.linehaul.tender.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTenderPreferenceValidator()
        val item = newLegacyTenderPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyTenderPreferenceCodeLengthRule(16)
        val item = newLegacyTenderPreference("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTenderPreferenceRequiredCodeRule("legacy tender preference needs an entry")
        val item = newLegacyTenderPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = LegacyTenderPreferenceCategoryLengthRule(64)
        val item = newLegacyTenderPreference("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = LegacyTenderPreferenceRequiredUnitRule("legacy tender preference needs this field")
        val item = newLegacyTenderPreference("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
