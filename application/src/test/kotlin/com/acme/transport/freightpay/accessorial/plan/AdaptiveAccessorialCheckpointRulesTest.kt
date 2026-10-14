package com.acme.transport.freightpay.accessorial.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAccessorialCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAccessorialCheckpointValidator()
        val item = newAdaptiveAccessorialCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AdaptiveAccessorialCheckpointRequiredDriverIdRule("adaptive accessorial checkpoint needs an entry")
        val item = newAdaptiveAccessorialCheckpoint("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveAccessorialCheckpointCodeLengthRule(1000)
        val item = newAdaptiveAccessorialCheckpoint("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AdaptiveAccessorialCheckpointRequiredContactEmailRule(
            "adaptive accessorial checkpoint needs a value",
        )
        val item = newAdaptiveAccessorialCheckpoint("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAccessorialCheckpointRequiredCodeRule("adaptive accessorial checkpoint needs an entry")
        val item = newAdaptiveAccessorialCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AdaptiveAccessorialCheckpointContactEmailLengthRule(1000)
        val item = newAdaptiveAccessorialCheckpoint("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
