package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDockDoorTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryDockDoorTemplate("code-807", "title-411")
        assertEquals(PrimaryDockDoorTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryDockDoorTemplate("code-807", "title-411")
        try {
            item.moveTo(PrimaryDockDoorTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDockDoorTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryDockDoorTemplate("code-807", "title-411")
        item.status = PrimaryDockDoorTemplateStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryDockDoorTemplateStatus.NEW) }
    }
}
