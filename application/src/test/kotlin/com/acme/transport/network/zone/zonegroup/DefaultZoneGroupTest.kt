package com.acme.transport.network.zone.zonegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultZoneGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultZoneGroup("code-54", "title-715")
        assertEquals(DefaultZoneGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultZoneGroup("code-54", "title-715")
        try {
            item.moveTo(DefaultZoneGroupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultZoneGroupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultZoneGroup("code-54", "title-715")
        item.status = DefaultZoneGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultZoneGroupStatus.REQUESTED) }
    }
}
