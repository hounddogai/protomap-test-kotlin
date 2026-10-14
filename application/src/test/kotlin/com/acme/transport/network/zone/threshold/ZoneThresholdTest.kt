package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newZoneThreshold("code-681", "label-941")
        assertEquals(ZoneThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newZoneThreshold("code-681", "label-941")
        try {
            item.moveTo(ZoneThresholdStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneThresholdStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newZoneThreshold("code-681", "label-941")
        item.status = ZoneThresholdStatus.FAILED
        assertFails("move") { item.moveTo(ZoneThresholdStatus.NEW) }
    }
}
