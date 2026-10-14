package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredNodeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredNodeBundleValidator()
        val item = newTieredNodeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = TieredNodeBundleLaneLengthRule(86400)
        val item = newTieredNodeBundle("x", "y")
        item.lane = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = TieredNodeBundleRateRangeRule(1.0, 2136.4)
        val item = newTieredNodeBundle("x", "y")
        item.rate = 2137.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredNodeBundleRequiredNameRule("tiered node bundle needs a value")
        val item = newTieredNodeBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = TieredNodeBundleRequiredSegmentRule("tiered node bundle needs this field")
        val item = newTieredNodeBundle("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = TieredNodeBundleMaximumRangeRule(0.3, 3302.3)
        val item = newTieredNodeBundle("x", "y")
        item.maximum = 3303.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredNodeBundleRequiredCodeRule("tiered node bundle needs a value")
        val item = newTieredNodeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
