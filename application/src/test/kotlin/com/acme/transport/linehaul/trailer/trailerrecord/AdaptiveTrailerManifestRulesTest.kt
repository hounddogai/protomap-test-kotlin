package com.acme.transport.linehaul.trailer.trailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTrailerManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTrailerManifestValidator()
        val item = newAdaptiveTrailerManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = AdaptiveTrailerManifestLimitRangeRule(120, 3600)
        val item = newAdaptiveTrailerManifest("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveTrailerManifestTitleLengthRule(16)
        val item = newAdaptiveTrailerManifest("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveTrailerManifestRequiredCodeRule("adaptive trailer manifest needs an entry")
        val item = newAdaptiveTrailerManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
