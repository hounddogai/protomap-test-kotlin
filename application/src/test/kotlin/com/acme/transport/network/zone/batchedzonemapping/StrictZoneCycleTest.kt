package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictZoneCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictZoneCycle("code-515", "label-713")
        assertEquals(StrictZoneCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newStrictZoneCycle("code-515", "label-713")
        try {
            item.moveTo(StrictZoneCycleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictZoneCycleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictZoneCycle("code-515", "label-713")
        item.status = StrictZoneCycleStatus.STOPPED
        assertFails("move") { item.moveTo(StrictZoneCycleStatus.IDLE) }
    }
}
