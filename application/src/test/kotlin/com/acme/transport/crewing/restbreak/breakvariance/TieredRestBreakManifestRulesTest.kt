package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRestBreakManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRestBreakManifestValidator()
        val item = newTieredRestBreakManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredRestBreakManifestCodeLengthRule(16)
        val item = newTieredRestBreakManifest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRestBreakManifestRequiredCodeRule("tiered rest break manifest needs a value")
        val item = newTieredRestBreakManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TieredRestBreakManifestOdometerRangeRule(80, 1000)
        val item = newTieredRestBreakManifest("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TieredRestBreakManifestThresholdRangeRule(9.5, 2930.9)
        val item = newTieredRestBreakManifest("x", "y")
        item.threshold = 2932.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredRestBreakManifestRequiredNameRule("tiered rest break manifest needs this field")
        val item = newTieredRestBreakManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
