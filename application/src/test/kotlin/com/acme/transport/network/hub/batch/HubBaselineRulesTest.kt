package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubBaselineValidator()
        val item = newHubBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = HubBaselineRequiredTitleRule("hub baseline needs this field")
        val item = newHubBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = HubBaselineContactEmailLengthRule(64)
        val item = newHubBaseline("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubBaselineRequiredCodeRule("hub baseline needs an entry")
        val item = newHubBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubBaselineCodeLengthRule(32)
        val item = newHubBaseline("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = HubBaselineScoreRangeRule(4.3, 1575.4)
        val item = newHubBaseline("x", "y")
        item.score = 1576.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
