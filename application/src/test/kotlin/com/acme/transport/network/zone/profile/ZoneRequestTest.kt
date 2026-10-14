package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneRequestTest {
    @Test
    fun `starts in new`() {
        val item = newZoneRequest("code-648", "name-355")
        assertEquals(ZoneRequestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newZoneRequest("code-648", "name-355")
        try {
            item.moveTo(ZoneRequestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneRequestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newZoneRequest("code-648", "name-355")
        item.status = ZoneRequestStatus.FAILED
        assertFails("move") { item.moveTo(ZoneRequestStatus.NEW) }
    }
}
