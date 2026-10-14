package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingZoneMappingTest {
    @Test
    fun `starts in created`() {
        val item = newPendingZoneMapping("code-271", "label-999")
        assertEquals(PendingZoneMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPendingZoneMapping("code-271", "label-999")
        try {
            item.moveTo(PendingZoneMappingStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingZoneMappingStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingZoneMapping("code-271", "label-999")
        item.status = PendingZoneMappingStatus.RETIRED
        assertFails("move") { item.moveTo(PendingZoneMappingStatus.CREATED) }
    }
}
