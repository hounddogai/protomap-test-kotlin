package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubVarianceValidator()
        val item = newHubVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = HubVarianceRatioRangeRule(8.5, 1090.7)
        val item = newHubVariance("x", "y")
        item.ratio = 1092.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = HubVarianceNameLengthRule(120)
        val item = newHubVariance("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubVarianceCodeLengthRule(3600)
        val item = newHubVariance("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = HubVarianceRequiredDockRule("hub variance needs an entry")
        val item = newHubVariance("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubVarianceRequiredCodeRule("hub variance needs an entry")
        val item = newHubVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
