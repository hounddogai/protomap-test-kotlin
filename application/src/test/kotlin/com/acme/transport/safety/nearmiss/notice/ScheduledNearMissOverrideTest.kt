package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNearMissOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledNearMissOverride("code-874", "name-302")
        assertEquals(ScheduledNearMissOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledNearMissOverride("code-874", "name-302")
        try {
            item.moveTo(ScheduledNearMissOverrideStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNearMissOverrideStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledNearMissOverride("code-874", "name-302")
        item.status = ScheduledNearMissOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledNearMissOverrideStatus.IDLE) }
    }
}
