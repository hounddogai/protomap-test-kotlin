package com.acme.transport.network.zone.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyZoneOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyZoneOverride("code-180", "title-564")
        assertEquals(LegacyZoneOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyZoneOverride("code-180", "title-564")
        try {
            item.moveTo(LegacyZoneOverrideStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyZoneOverrideStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyZoneOverride("code-180", "title-564")
        item.status = LegacyZoneOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyZoneOverrideStatus.REQUESTED) }
    }
}
