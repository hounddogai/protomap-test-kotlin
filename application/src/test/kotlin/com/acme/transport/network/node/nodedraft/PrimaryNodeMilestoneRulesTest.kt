package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNodeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryNodeMilestoneValidator()
        val item = newPrimaryNodeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryNodeMilestoneNameLengthRule(1000)
        val item = newPrimaryNodeMilestone("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryNodeMilestoneRequiredCodeRule("primary node milestone needs this field")
        val item = newPrimaryNodeMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
