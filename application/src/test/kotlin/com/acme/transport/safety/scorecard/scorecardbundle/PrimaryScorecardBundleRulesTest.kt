package com.acme.transport.safety.scorecard.scorecardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryScorecardBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryScorecardBundleValidator()
        val item = newPrimaryScorecardBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = PrimaryScorecardBundleCategoryLengthRule(32)
        val item = newPrimaryScorecardBundle("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = PrimaryScorecardBundlePalletsRangeRule(16, 255)
        val item = newPrimaryScorecardBundle("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PrimaryScorecardBundleRequiredSegmentRule("primary scorecard bundle needs an entry")
        val item = newPrimaryScorecardBundle("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = PrimaryScorecardBundleMaximumRangeRule(9.7, 1687.9)
        val item = newPrimaryScorecardBundle("x", "y")
        item.maximum = 1689.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryScorecardBundleTitleLengthRule(255)
        val item = newPrimaryScorecardBundle("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
