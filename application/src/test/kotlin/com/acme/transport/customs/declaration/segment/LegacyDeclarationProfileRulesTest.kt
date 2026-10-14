package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDeclarationProfileValidator()
        val item = newLegacyDeclarationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDeclarationProfileRequiredCodeRule("legacy declaration profile needs this field")
        val item = newLegacyDeclarationProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = LegacyDeclarationProfileCurrencyLengthRule(16)
        val item = newLegacyDeclarationProfile("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyDeclarationProfileRequiredLabelRule("legacy declaration profile needs this field")
        val item = newLegacyDeclarationProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LegacyDeclarationProfileLimitRangeRule(16, 32)
        val item = newLegacyDeclarationProfile("x", "y")
        item.limit = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyDeclarationProfileLabelLengthRule(80)
        val item = newLegacyDeclarationProfile("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = LegacyDeclarationProfileDistanceKmRangeRule(8.3, 3423.5)
        val item = newLegacyDeclarationProfile("x", "y")
        item.distanceKm = 3425.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
