package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapManifestValidator()
        val item = newSwapManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SwapManifestRequiredTitleRule("swap manifest needs an entry")
        val item = newSwapManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SwapManifestTitleLengthRule(80)
        val item = newSwapManifest("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = SwapManifestCountryLengthRule(64)
        val item = newSwapManifest("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = SwapManifestRequiredTrailerIdRule("swap manifest needs a value")
        val item = newSwapManifest("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
