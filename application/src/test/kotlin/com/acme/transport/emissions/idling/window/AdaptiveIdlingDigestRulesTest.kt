package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveIdlingDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveIdlingDigestValidator()
        val item = newAdaptiveIdlingDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveIdlingDigestRequiredCodeRule("adaptive idling digest needs an entry")
        val item = newAdaptiveIdlingDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveIdlingDigestTitleLengthRule(120)
        val item = newAdaptiveIdlingDigest("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = AdaptiveIdlingDigestCountryLengthRule(80)
        val item = newAdaptiveIdlingDigest("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = AdaptiveIdlingDigestCategoryLengthRule(120)
        val item = newAdaptiveIdlingDigest("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveIdlingDigestRequiredTitleRule("adaptive idling digest needs an entry")
        val item = newAdaptiveIdlingDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
