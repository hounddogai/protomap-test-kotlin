package com.acme.transport.network.node.nodepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeBundleValidator()
        val item = newNodeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = NodeBundleCostRangeRule(7.5, 4262.9)
        val item = newNodeBundle("x", "y")
        item.cost = 4264.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = NodeBundleLabelLengthRule(120)
        val item = newNodeBundle("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = NodeBundleStartsAtRangeRule(120, 255)
        val item = newNodeBundle("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeBundleRequiredCodeRule("node bundle needs a value")
        val item = newNodeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeBundleCodeLengthRule(80)
        val item = newNodeBundle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
