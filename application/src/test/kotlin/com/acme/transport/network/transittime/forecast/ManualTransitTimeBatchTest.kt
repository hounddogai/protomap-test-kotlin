package com.acme.transport.network.transittime.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTransitTimeBatchTest {
    @Test
    fun `starts in idle`() {
        val item = newManualTransitTimeBatch("code-375", "title-527")
        assertEquals(ManualTransitTimeBatchStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newManualTransitTimeBatch("code-375", "title-527")
        try {
            item.moveTo(ManualTransitTimeBatchStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTransitTimeBatchStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualTransitTimeBatch("code-375", "title-527")
        item.status = ManualTransitTimeBatchStatus.STOPPED
        assertFails("move") { item.moveTo(ManualTransitTimeBatchStatus.IDLE) }
    }
}
