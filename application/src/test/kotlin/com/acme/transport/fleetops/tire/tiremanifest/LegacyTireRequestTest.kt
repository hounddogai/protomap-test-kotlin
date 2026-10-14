package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTireRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyTireRequest("code-609", "name-689")
        assertEquals(LegacyTireRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyTireRequest("code-609", "name-689")
        try {
            item.moveTo(LegacyTireRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTireRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyTireRequest("code-609", "name-689")
        item.status = LegacyTireRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyTireRequestStatus.REQUESTED) }
    }
}
