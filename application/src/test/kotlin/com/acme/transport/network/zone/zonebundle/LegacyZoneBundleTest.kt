package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyZoneBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyZoneBundle("code-506", "name-860")
        assertEquals(LegacyZoneBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyZoneBundle("code-506", "name-860")
        try {
            item.moveTo(LegacyZoneBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyZoneBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyZoneBundle("code-506", "name-860")
        item.status = LegacyZoneBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyZoneBundleStatus.PROPOSED) }
    }
}
