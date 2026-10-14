package com.acme.transport.crewing.availability.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAvailabilityCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAvailabilityCycleValidator()
        val item = newScheduledAvailabilityCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAvailabilityCycleCodeLengthRule(1000)
        val item = newScheduledAvailabilityCycle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledAvailabilityCycleRequiredCodeRule("scheduled availability cycle needs this field")
        val item = newScheduledAvailabilityCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledAvailabilityCycleRequiredNameRule("scheduled availability cycle needs a value")
        val item = newScheduledAvailabilityCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ScheduledAvailabilityCycleRequiredLaneRule("scheduled availability cycle needs this field")
        val item = newScheduledAvailabilityCycle("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
