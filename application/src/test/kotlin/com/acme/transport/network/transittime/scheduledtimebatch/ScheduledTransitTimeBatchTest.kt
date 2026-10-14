package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTransitTimeBatchTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledTransitTimeBatch("code-754", "name-424")
        assertEquals(ScheduledTransitTimeBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newScheduledTransitTimeBatch("code-754", "name-424")
        try {
            item.moveTo(ScheduledTransitTimeBatchStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTransitTimeBatchStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledTransitTimeBatch("code-754", "name-424")
        item.status = ScheduledTransitTimeBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledTransitTimeBatchStatus.OPEN) }
    }
}
