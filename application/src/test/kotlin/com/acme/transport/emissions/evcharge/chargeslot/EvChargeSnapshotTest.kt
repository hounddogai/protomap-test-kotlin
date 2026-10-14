package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newEvChargeSnapshot("code-409", "label-378")
        assertEquals(EvChargeSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newEvChargeSnapshot("code-409", "label-378")
        try {
            item.moveTo(EvChargeSnapshotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeSnapshotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEvChargeSnapshot("code-409", "label-378")
        item.status = EvChargeSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(EvChargeSnapshotStatus.CREATED) }
    }
}
