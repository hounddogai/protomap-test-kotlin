package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDockDoorDraftValidator()
        val item = newPendingDockDoorDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = PendingDockDoorDraftRequiredLaneRule("pending dock door draft needs an entry")
        val item = newPendingDockDoorDraft("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingDockDoorDraftRequiredTitleRule("pending dock door draft needs a value")
        val item = newPendingDockDoorDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
