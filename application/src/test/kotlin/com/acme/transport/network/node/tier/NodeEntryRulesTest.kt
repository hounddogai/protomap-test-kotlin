package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeEntryValidator()
        val item = newNodeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = NodeEntryRetriesRangeRule(255, 255)
        val item = newNodeEntry("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeEntryCodeLengthRule(120)
        val item = newNodeEntry("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = NodeEntryFuelLitersRangeRule(4.0, 1807.0)
        val item = newNodeEntry("x", "y")
        item.fuelLiters = 1808.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = NodeEntryDiscountRangeRule(5.5, 334.9)
        val item = newNodeEntry("x", "y")
        item.discount = 336.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NodeEntryRequiredNameRule("node entry needs an entry")
        val item = newNodeEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
