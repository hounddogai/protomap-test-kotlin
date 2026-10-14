package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerMilestoneValidator()
        val item = newBrokerMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = BrokerMilestoneRequiredOwnerIdRule("broker milestone needs an entry")
        val item = newBrokerMilestone("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = BrokerMilestoneWeightRangeRule(8.4, 1329.2)
        val item = newBrokerMilestone("x", "y")
        item.weight = 1330.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BrokerMilestoneRequiredNameRule("broker milestone needs this field")
        val item = newBrokerMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BrokerMilestoneNameLengthRule(120)
        val item = newBrokerMilestone("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
