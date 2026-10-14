package com.acme.transport.customs.clearance.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newClearanceRequest("code-311", "name-7")
        assertEquals(ClearanceRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newClearanceRequest("code-311", "name-7")
        try {
            item.moveTo(ClearanceRequestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceRequestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newClearanceRequest("code-311", "name-7")
        item.status = ClearanceRequestStatus.STOPPED
        assertFails("move") { item.moveTo(ClearanceRequestStatus.IDLE) }
    }
}
