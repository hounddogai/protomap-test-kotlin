package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newRouteOffsetRecord("code-842", "name-668")
        assertEquals(RouteOffsetRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRouteOffsetRecord("code-842", "name-668")
        try {
            item.moveTo(RouteOffsetRecordStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetRecordStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRouteOffsetRecord("code-842", "name-668")
        item.status = RouteOffsetRecordStatus.STOPPED
        assertFails("move") { item.moveTo(RouteOffsetRecordStatus.IDLE) }
    }
}
