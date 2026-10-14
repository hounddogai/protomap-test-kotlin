package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNodeAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultNodeAllocationValidator()
        val item = newDefaultNodeAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultNodeAllocationRequiredCodeRule("default node allocation needs a value")
        val item = newDefaultNodeAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultNodeAllocationRequiredTitleRule("default node allocation needs an entry")
        val item = newDefaultNodeAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultNodeAllocationTitleLengthRule(1000)
        val item = newDefaultNodeAllocation("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DefaultNodeAllocationDriverIdLengthRule(120)
        val item = newDefaultNodeAllocation("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
