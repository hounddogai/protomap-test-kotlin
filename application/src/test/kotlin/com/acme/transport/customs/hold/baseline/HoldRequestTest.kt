package com.acme.transport.customs.hold.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newHoldRequest("code-162", "name-679")
        assertEquals(HoldRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newHoldRequest("code-162", "name-679")
        try {
            item.moveTo(HoldRequestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldRequestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHoldRequest("code-162", "name-679")
        item.status = HoldRequestStatus.STOPPED
        assertFails("move") { item.moveTo(HoldRequestStatus.IDLE) }
    }
}
