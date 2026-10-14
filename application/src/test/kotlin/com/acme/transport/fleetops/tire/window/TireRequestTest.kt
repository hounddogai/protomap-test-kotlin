package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newTireRequest("code-984", "title-185")
        assertEquals(TireRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTireRequest("code-984", "title-185")
        try {
            item.moveTo(TireRequestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireRequestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTireRequest("code-984", "title-185")
        item.status = TireRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(TireRequestStatus.REQUESTED) }
    }
}
