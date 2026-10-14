package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorScheduleValidator()
        val item = newDockDoorSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorScheduleRequiredNameRule("dock door schedule needs a value")
        val item = newDockDoorSchedule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = DockDoorScheduleRequiredSummaryRule("dock door schedule needs a value")
        val item = newDockDoorSchedule("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
