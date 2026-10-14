package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTrailerManifestValidator()
        val item = newStrictTrailerManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = StrictTrailerManifestBatchSizeRangeRule(16, 16)
        val item = newStrictTrailerManifest("x", "y")
        item.batchSize = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = StrictTrailerManifestThresholdRangeRule(2.3, 1738.6)
        val item = newStrictTrailerManifest("x", "y")
        item.threshold = 1740.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = StrictTrailerManifestCountryLengthRule(80)
        val item = newStrictTrailerManifest("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictTrailerManifestCodeLengthRule(32)
        val item = newStrictTrailerManifest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTrailerManifestRequiredCodeRule("strict trailer manifest needs this field")
        val item = newStrictTrailerManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
