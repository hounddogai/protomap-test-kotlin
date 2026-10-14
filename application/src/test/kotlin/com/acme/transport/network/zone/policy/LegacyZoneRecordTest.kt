package com.acme.transport.network.zone.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyZoneRecordTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyZoneRecord("code-925", "title-330")
        assertEquals(LegacyZoneRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyZoneRecord("code-925", "title-330")
        try {
            item.moveTo(LegacyZoneRecordStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyZoneRecordStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyZoneRecord("code-925", "title-330")
        item.status = LegacyZoneRecordStatus.FAILED
        assertFails("move") { item.moveTo(LegacyZoneRecordStatus.NEW) }
    }
}
