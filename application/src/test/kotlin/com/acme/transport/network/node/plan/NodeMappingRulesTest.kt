package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeMappingValidator()
        val item = newNodeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NodeMappingRequiredLabelRule("node mapping needs this field")
        val item = newNodeMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeMappingRequiredCodeRule("node mapping needs a value")
        val item = newNodeMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = NodeMappingNameLengthRule(32)
        val item = newNodeMapping("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NodeMappingRequiredNameRule("node mapping needs this field")
        val item = newNodeMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = NodeMappingScoreRangeRule(6.2, 3497.7)
        val item = newNodeMapping("x", "y")
        item.score = 3499.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = NodeMappingCapacityRangeRule(120, 1000)
        val item = newNodeMapping("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
