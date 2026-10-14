package com.acme.transport.network.zone.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackZoneThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackZoneThreshold("code-690", "name-713")
        assertEquals(FallbackZoneThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFallbackZoneThreshold("code-690", "name-713")
        try {
            item.moveTo(FallbackZoneThresholdStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackZoneThresholdStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackZoneThreshold("code-690", "name-713")
        item.status = FallbackZoneThresholdStatus.FAILED
        assertFails("move") { item.moveTo(FallbackZoneThresholdStatus.NEW) }
    }
}
