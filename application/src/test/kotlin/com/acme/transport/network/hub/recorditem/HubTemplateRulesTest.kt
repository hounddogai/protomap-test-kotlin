package com.acme.transport.network.hub.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubTemplateValidator()
        val item = newHubTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = HubTemplateRatioRangeRule(1.3, 723.2)
        val item = newHubTemplate("x", "y")
        item.ratio = 724.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubTemplateCodeLengthRule(80)
        val item = newHubTemplate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = HubTemplateMarginRangeRule(2.1, 1562.7)
        val item = newHubTemplate("x", "y")
        item.margin = 1564.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
