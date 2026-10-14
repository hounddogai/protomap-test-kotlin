package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDockDoorAuditTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryDockDoorAudit("code-357", "label-314")
        assertEquals(PrimaryDockDoorAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryDockDoorAudit("code-357", "label-314")
        try {
            item.moveTo(PrimaryDockDoorAuditStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDockDoorAuditStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryDockDoorAudit("code-357", "label-314")
        item.status = PrimaryDockDoorAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryDockDoorAuditStatus.OPEN) }
    }
}
