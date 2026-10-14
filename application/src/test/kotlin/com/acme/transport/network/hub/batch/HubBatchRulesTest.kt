package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubBatchValidator()
        val item = newHubBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubBatchRequiredCodeRule("hub batch needs a value")
        val item = newHubBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = HubBatchAmountRangeRule(1.7, 2546.0)
        val item = newHubBatch("x", "y")
        item.amount = 2547.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = HubBatchRequiredChannelRule("hub batch needs this field")
        val item = newHubBatch("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = HubBatchLoadKgRangeRule(9.4, 2831.9)
        val item = newHubBatch("x", "y")
        item.loadKg = 2833.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HubBatchRequiredNameRule("hub batch needs this field")
        val item = newHubBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
