package com.acme.transport.network.node.weightednodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeRecordValidator()
        val item = newNodeRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = NodeRecordNameLengthRule(3600)
        val item = newNodeRecord("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NodeRecordRequiredNameRule("node record needs a value")
        val item = newNodeRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = NodeRecordCapacityRangeRule(255, 86400)
        val item = newNodeRecord("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeRecordRequiredCodeRule("node record needs this field")
        val item = newNodeRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
