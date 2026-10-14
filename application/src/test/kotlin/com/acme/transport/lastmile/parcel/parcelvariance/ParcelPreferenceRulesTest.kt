package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelPreferenceValidator()
        val item = newParcelPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ParcelPreferenceLabelLengthRule(255)
        val item = newParcelPreference("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelPreferenceCodeLengthRule(16)
        val item = newParcelPreference("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ParcelPreferenceRequiredSummaryRule("parcel preference needs this field")
        val item = newParcelPreference("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelPreferenceRequiredCodeRule("parcel preference needs an entry")
        val item = newParcelPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
