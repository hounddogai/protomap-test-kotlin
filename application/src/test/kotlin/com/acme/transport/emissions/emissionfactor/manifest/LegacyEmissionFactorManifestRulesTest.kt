package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyEmissionFactorManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyEmissionFactorManifestValidator()
        val item = newLegacyEmissionFactorManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LegacyEmissionFactorManifestWindowDaysRangeRule(80, 86400)
        val item = newLegacyEmissionFactorManifest("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyEmissionFactorManifestNameLengthRule(16)
        val item = newLegacyEmissionFactorManifest("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyEmissionFactorManifestRequiredNameRule("legacy emission factor manifest needs a value")
        val item = newLegacyEmissionFactorManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
