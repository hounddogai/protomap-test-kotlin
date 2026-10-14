package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newDockDoorAudit("code-835", "name-28")
        assertEquals(DockDoorAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDockDoorAudit("code-835", "name-28")
        try {
            item.moveTo(DockDoorAuditStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorAuditStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDockDoorAudit("code-835", "name-28")
        item.status = DockDoorAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(DockDoorAuditStatus.REQUESTED) }
    }
}
