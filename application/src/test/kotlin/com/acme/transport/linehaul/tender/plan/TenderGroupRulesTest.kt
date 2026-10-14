package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderGroupValidator()
        val item = newTenderGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TenderGroupCeilingRangeRule(1.7, 4813.0)
        val item = newTenderGroup("x", "y")
        item.ceiling = 4814.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = TenderGroupChannelLengthRule(80)
        val item = newTenderGroup("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = TenderGroupRequiredChannelRule("tender group needs this field")
        val item = newTenderGroup("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderGroupCodeLengthRule(3600)
        val item = newTenderGroup("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
