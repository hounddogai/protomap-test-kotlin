package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newHoldSnapshot("code-237", "label-901")
        assertEquals(HoldSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newHoldSnapshot("code-237", "label-901")
        try {
            item.moveTo(HoldSnapshotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldSnapshotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHoldSnapshot("code-237", "label-901")
        item.status = HoldSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(HoldSnapshotStatus.PLANNED) }
    }
}
