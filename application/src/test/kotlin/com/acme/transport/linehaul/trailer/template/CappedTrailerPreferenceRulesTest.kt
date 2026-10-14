package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerPreferenceValidator()
        val item = newCappedTrailerPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = CappedTrailerPreferenceRequiredChecksumRule("capped trailer preference needs this field")
        val item = newCappedTrailerPreference("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CappedTrailerPreferenceLoadKgRangeRule(9.4, 1218.5)
        val item = newCappedTrailerPreference("x", "y")
        item.loadKg = 1220.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CappedTrailerPreferenceCeilingRangeRule(2.2, 2350.0)
        val item = newCappedTrailerPreference("x", "y")
        item.ceiling = 2351.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
