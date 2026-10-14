package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newEvChargeTarget("code-234", "label-24")
        assertEquals(EvChargeTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newEvChargeTarget("code-234", "label-24")
        try {
            item.moveTo(EvChargeTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEvChargeTarget("code-234", "label-24")
        item.status = EvChargeTargetStatus.STOPPED
        assertFails("move") { item.moveTo(EvChargeTargetStatus.IDLE) }
    }
}
