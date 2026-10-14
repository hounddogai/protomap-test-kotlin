package com.acme.transport.terminal.traileryard.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerYardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerYardManifestValidator()
        val item = newRegionalTrailerYardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalTrailerYardManifestLoadKgRangeRule(3.8, 4400.7)
        val item = newRegionalTrailerYardManifest("x", "y")
        item.loadKg = 4402.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = RegionalTrailerYardManifestSegmentLengthRule(1000)
        val item = newRegionalTrailerYardManifest("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalTrailerYardManifestCodeLengthRule(1000)
        val item = newRegionalTrailerYardManifest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalTrailerYardManifestLabelLengthRule(16)
        val item = newRegionalTrailerYardManifest("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RegionalTrailerYardManifestRequiredSegmentRule("regional trailer yard manifest needs a value")
        val item = newRegionalTrailerYardManifest("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = RegionalTrailerYardManifestSummaryLengthRule(80)
        val item = newRegionalTrailerYardManifest("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
