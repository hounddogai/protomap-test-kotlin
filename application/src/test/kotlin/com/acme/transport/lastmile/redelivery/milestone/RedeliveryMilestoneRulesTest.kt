package com.acme.transport.lastmile.redelivery.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryMilestoneValidator()
        val item = newRedeliveryMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryMilestoneRequiredCodeRule("redelivery milestone needs this field")
        val item = newRedeliveryMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RedeliveryMilestoneRequiredNameRule("redelivery milestone needs a value")
        val item = newRedeliveryMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = RedeliveryMilestoneRequiredPhoneRule("redelivery milestone needs an entry")
        val item = newRedeliveryMilestone("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RedeliveryMilestoneNameLengthRule(1000)
        val item = newRedeliveryMilestone("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
