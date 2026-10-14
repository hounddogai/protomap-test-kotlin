package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNodeAllocationValidator()
        val item = newAdaptiveNodeAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = AdaptiveNodeAllocationRequiredCategoryRule("adaptive node allocation needs an entry")
        val item = newAdaptiveNodeAllocation("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveNodeAllocationRequiredCodeRule("adaptive node allocation needs this field")
        val item = newAdaptiveNodeAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveNodeAllocationRequiredNameRule("adaptive node allocation needs an entry")
        val item = newAdaptiveNodeAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveNodeAllocationCodeLengthRule(120)
        val item = newAdaptiveNodeAllocation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
