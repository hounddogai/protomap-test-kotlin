package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingClearanceRecordTest {
    @Test
    fun `starts in created`() {
        val item = newPendingClearanceRecord("code-334", "name-51")
        assertEquals(PendingClearanceRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingClearanceRecord("code-334", "name-51")
        try {
            item.moveTo(PendingClearanceRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingClearanceRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingClearanceRecord("code-334", "name-51")
        item.status = PendingClearanceRecordStatus.RETIRED
        assertFails("move") { item.moveTo(PendingClearanceRecordStatus.CREATED) }
    }
}
