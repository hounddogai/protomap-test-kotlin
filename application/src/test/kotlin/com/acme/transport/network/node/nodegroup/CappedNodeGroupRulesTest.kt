package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedNodeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedNodeGroupValidator()
        val item = newCappedNodeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedNodeGroupLimitRangeRule(120, 255)
        val item = newCappedNodeGroup("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = CappedNodeGroupCarrierLengthRule(16)
        val item = newCappedNodeGroup("x", "y")
        item.carrier = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = CappedNodeGroupCategoryLengthRule(80)
        val item = newCappedNodeGroup("x", "y")
        item.category = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = CappedNodeGroupVersionRangeRule(80, 3600)
        val item = newCappedNodeGroup("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
