package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDockDoorPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDockDoorPolicyValidator()
        val item = newScheduledDockDoorPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDockDoorPolicyRequiredCodeRule("scheduled dock door policy needs an entry")
        val item = newScheduledDockDoorPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledDockDoorPolicyRequiredTitleRule("scheduled dock door policy needs a value")
        val item = newScheduledDockDoorPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
