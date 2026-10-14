package com.acme.transport.fleetops.tire.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newTireException("code-548", "label-668")
        assertEquals(TireExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTireException("code-548", "label-668")
        try {
            item.moveTo(TireExceptionStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireExceptionStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTireException("code-548", "label-668")
        item.status = TireExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(TireExceptionStatus.IDLE) }
    }
}
