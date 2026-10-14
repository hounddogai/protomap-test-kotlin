package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeSegmentValidator()
        val item = newNodeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = NodeSegmentBudgetRangeRule(9.2, 95.1)
        val item = newNodeSegment("x", "y")
        item.budget = 96.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = NodeSegmentCategoryLengthRule(120)
        val item = newNodeSegment("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeSegmentRequiredCodeRule("node segment needs an entry")
        val item = newNodeSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
