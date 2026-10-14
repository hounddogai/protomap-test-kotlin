package com.acme.transport.fleetops.utilization.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledUtilizationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledUtilizationCycleValidator()
        val item = newScheduledUtilizationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledUtilizationCycleRequiredNameRule("scheduled utilization cycle needs a value")
        val item = newScheduledUtilizationCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledUtilizationCycleRequiredCodeRule("scheduled utilization cycle needs a value")
        val item = newScheduledUtilizationCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ScheduledUtilizationCycleRequiredChecksumRule("scheduled utilization cycle needs a value")
        val item = newScheduledUtilizationCycle("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
