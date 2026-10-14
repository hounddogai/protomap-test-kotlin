package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeWindowTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeWindow("code-720", "label-664")
        assertEquals(EvChargeWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newEvChargeWindow("code-720", "label-664")
        try {
            item.moveTo(EvChargeWindowStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeWindowStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeWindow("code-720", "label-664")
        item.status = EvChargeWindowStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeWindowStatus.NEW) }
    }
}
