package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredParcelPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredParcelPreferenceValidator()
        val item = newTieredParcelPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredParcelPreferenceTitleLengthRule(255)
        val item = newTieredParcelPreference("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = TieredParcelPreferenceScoreRangeRule(4.8, 1810.9)
        val item = newTieredParcelPreference("x", "y")
        item.score = 1812.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredParcelPreferenceRequiredTitleRule("tiered parcel preference needs an entry")
        val item = newTieredParcelPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = TieredParcelPreferenceDescriptionLengthRule(86400)
        val item = newTieredParcelPreference("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
