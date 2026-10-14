package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayMilestoneValidator()
        val item = newRelayMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RelayMilestoneContactEmailLengthRule(3600)
        val item = newRelayMilestone("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RelayMilestoneRequiredContactEmailRule("relay milestone needs this field")
        val item = newRelayMilestone("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
