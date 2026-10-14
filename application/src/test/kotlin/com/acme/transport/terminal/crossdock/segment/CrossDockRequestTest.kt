package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newCrossDockRequest("code-646", "label-587")
        assertEquals(CrossDockRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCrossDockRequest("code-646", "label-587")
        try {
            item.moveTo(CrossDockRequestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockRequestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCrossDockRequest("code-646", "label-587")
        item.status = CrossDockRequestStatus.STOPPED
        assertFails("move") { item.moveTo(CrossDockRequestStatus.IDLE) }
    }
}
