package com.acme.transport.network.zone.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientZoneGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientZoneGroup("code-740", "name-882")
        assertEquals(LenientZoneGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientZoneGroup("code-740", "name-882")
        try {
            item.moveTo(LenientZoneGroupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientZoneGroupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientZoneGroup("code-740", "name-882")
        item.status = LenientZoneGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientZoneGroupStatus.PLANNED) }
    }
}
