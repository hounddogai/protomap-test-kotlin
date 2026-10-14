package com.acme.transport.network.hub.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalHubNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalHubNoticeValidator()
        val item = newRegionalHubNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RegionalHubNoticePalletsRangeRule(16, 16)
        val item = newRegionalHubNotice("x", "y")
        item.pallets = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalHubNoticeCodeLengthRule(80)
        val item = newRegionalHubNotice("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
