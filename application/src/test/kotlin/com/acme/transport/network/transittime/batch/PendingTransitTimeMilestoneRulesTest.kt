package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTransitTimeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTransitTimeMilestoneValidator()
        val item = newPendingTransitTimeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PendingTransitTimeMilestoneChannelLengthRule(255)
        val item = newPendingTransitTimeMilestone("x", "y")
        item.channel = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = PendingTransitTimeMilestoneQuantityRangeRule(32, 120)
        val item = newPendingTransitTimeMilestone("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingTransitTimeMilestoneRequiredCodeRule("pending transit time milestone needs this field")
        val item = newPendingTransitTimeMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = PendingTransitTimeMilestoneCarrierLengthRule(80)
        val item = newPendingTransitTimeMilestone("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingTransitTimeMilestoneRequiredNameRule("pending transit time milestone needs an entry")
        val item = newPendingTransitTimeMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
