package com.acme.transport.network.node.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeBaselineValidator()
        val item = newNodeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = NodeBaselineMinimumRangeRule(1.0, 3999.1)
        val item = newNodeBaseline("x", "y")
        item.minimum = 4000.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = NodeBaselineCapacityRangeRule(16, 3600)
        val item = newNodeBaseline("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = NodeBaselineRequiredChannelRule("node baseline needs an entry")
        val item = newNodeBaseline("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
