package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyZoneProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyZoneProfile("code-481", "label-344")
        assertEquals(LegacyZoneProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLegacyZoneProfile("code-481", "label-344")
        try {
            item.moveTo(LegacyZoneProfileStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyZoneProfileStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyZoneProfile("code-481", "label-344")
        item.status = LegacyZoneProfileStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyZoneProfileStatus.DRAFT) }
    }
}
