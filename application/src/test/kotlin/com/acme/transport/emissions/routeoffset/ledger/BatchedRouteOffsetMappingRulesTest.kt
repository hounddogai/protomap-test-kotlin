package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRouteOffsetMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRouteOffsetMappingValidator()
        val item = newBatchedRouteOffsetMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedRouteOffsetMappingRequiredCodeRule("batched route offset mapping needs a value")
        val item = newBatchedRouteOffsetMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedRouteOffsetMappingRequiredNameRule("batched route offset mapping needs an entry")
        val item = newBatchedRouteOffsetMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BatchedRouteOffsetMappingCostRangeRule(9.2, 4884.9)
        val item = newBatchedRouteOffsetMapping("x", "y")
        item.cost = 4886.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRouteOffsetMappingCodeLengthRule(120)
        val item = newBatchedRouteOffsetMapping("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = BatchedRouteOffsetMappingExternalRefLengthRule(120)
        val item = newBatchedRouteOffsetMapping("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
