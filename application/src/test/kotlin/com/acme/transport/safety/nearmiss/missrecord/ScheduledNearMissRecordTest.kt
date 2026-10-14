package com.acme.transport.safety.nearmiss.missrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNearMissRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledNearMissRecord("code-57", "label-568")
        assertEquals(ScheduledNearMissRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledNearMissRecord("code-57", "label-568")
        try {
            item.moveTo(ScheduledNearMissRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNearMissRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledNearMissRecord("code-57", "label-568")
        item.status = ScheduledNearMissRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledNearMissRecordStatus.PROPOSED) }
    }
}
