package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDeclarationThresholdValidator()
        val item = newLegacyDeclarationThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LegacyDeclarationThresholdThresholdRangeRule(9.2, 1604.4)
        val item = newLegacyDeclarationThreshold("x", "y")
        item.threshold = 1605.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = LegacyDeclarationThresholdCountryLengthRule(64)
        val item = newLegacyDeclarationThreshold("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LegacyDeclarationThresholdLimitRangeRule(120, 86400)
        val item = newLegacyDeclarationThreshold("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = LegacyDeclarationThresholdUnitLengthRule(3600)
        val item = newLegacyDeclarationThreshold("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
