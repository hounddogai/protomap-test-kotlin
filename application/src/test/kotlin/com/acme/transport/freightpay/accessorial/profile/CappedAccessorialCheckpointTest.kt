package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAccessorialCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedAccessorialCheckpoint("code-590", "name-747")
        assertEquals(CappedAccessorialCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newCappedAccessorialCheckpoint("code-590", "name-747")
        try {
            item.moveTo(CappedAccessorialCheckpointStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAccessorialCheckpointStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedAccessorialCheckpoint("code-590", "name-747")
        item.status = CappedAccessorialCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(CappedAccessorialCheckpointStatus.IDLE) }
    }
}
