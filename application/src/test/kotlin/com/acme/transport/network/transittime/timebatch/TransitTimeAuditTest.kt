package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newTransitTimeAudit("code-734", "name-2")
        assertEquals(TransitTimeAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTransitTimeAudit("code-734", "name-2")
        try {
            item.moveTo(TransitTimeAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTransitTimeAudit("code-734", "name-2")
        item.status = TransitTimeAuditStatus.STOPPED
        assertFails("move") { item.moveTo(TransitTimeAuditStatus.IDLE) }
    }
}
