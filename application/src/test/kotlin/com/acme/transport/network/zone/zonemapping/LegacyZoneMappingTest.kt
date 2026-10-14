package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyZoneMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyZoneMapping("code-759", "label-256")
        assertEquals(LegacyZoneMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLegacyZoneMapping("code-759", "label-256")
        try {
            item.moveTo(LegacyZoneMappingStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyZoneMappingStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyZoneMapping("code-759", "label-256")
        item.status = LegacyZoneMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyZoneMappingStatus.PLANNED) }
    }
}
