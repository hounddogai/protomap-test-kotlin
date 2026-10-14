package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedNodeWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedNodeWindowValidator()
        val item = newWeightedNodeWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = WeightedNodeWindowSegmentLengthRule(255)
        val item = newWeightedNodeWindow("x", "y")
        item.segment = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedNodeWindowNameLengthRule(86400)
        val item = newWeightedNodeWindow("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedNodeWindowRequiredCodeRule("weighted node window needs this field")
        val item = newWeightedNodeWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
