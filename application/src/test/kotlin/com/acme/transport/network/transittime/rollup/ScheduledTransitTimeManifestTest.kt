package com.acme.transport.network.transittime.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTransitTimeManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledTransitTimeManifest("code-84", "name-290")
        assertEquals(ScheduledTransitTimeManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newScheduledTransitTimeManifest("code-84", "name-290")
        try {
            item.moveTo(ScheduledTransitTimeManifestStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTransitTimeManifestStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledTransitTimeManifest("code-84", "name-290")
        item.status = ScheduledTransitTimeManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledTransitTimeManifestStatus.PLANNED) }
    }
}
