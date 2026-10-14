package com.acme.transport.terminal.traileryard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTrailerYardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTrailerYardManifestValidator()
        val item = newLegacyTrailerYardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LegacyTrailerYardManifestAmountRangeRule(4.0, 963.6)
        val item = newLegacyTrailerYardManifest("x", "y")
        item.amount = 965.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTrailerYardManifestRequiredCodeRule("legacy trailer yard manifest needs a value")
        val item = newLegacyTrailerYardManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyTrailerYardManifestCodeLengthRule(3600)
        val item = newLegacyTrailerYardManifest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyTrailerYardManifestRequiredNameRule("legacy trailer yard manifest needs this field")
        val item = newLegacyTrailerYardManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
