package com.acme.transport.terminal.dockdoor.tiereddooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDockDoorTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingDockDoorTemplate("code-304", "title-219")
        assertEquals(RollingDockDoorTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingDockDoorTemplate("code-304", "title-219")
        try {
            item.moveTo(RollingDockDoorTemplateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDockDoorTemplateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingDockDoorTemplate("code-304", "title-219")
        item.status = RollingDockDoorTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingDockDoorTemplateStatus.PLANNED) }
    }
}
