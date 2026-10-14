package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneAudit("code-611", "name-99")
        assertEquals(ZoneAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newZoneAudit("code-611", "name-99")
        try {
            item.moveTo(ZoneAuditStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneAuditStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneAudit("code-611", "name-99")
        item.status = ZoneAuditStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneAuditStatus.DRAFT) }
    }
}
