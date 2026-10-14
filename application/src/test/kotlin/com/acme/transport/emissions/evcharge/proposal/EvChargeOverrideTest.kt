package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeOverride("code-179", "label-469")
        assertEquals(EvChargeOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newEvChargeOverride("code-179", "label-469")
        try {
            item.moveTo(EvChargeOverrideStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeOverrideStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeOverride("code-179", "label-469")
        item.status = EvChargeOverrideStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeOverrideStatus.NEW) }
    }
}
