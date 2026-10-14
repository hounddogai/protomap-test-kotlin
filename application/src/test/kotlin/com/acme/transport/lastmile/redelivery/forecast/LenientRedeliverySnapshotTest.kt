package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRedeliverySnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientRedeliverySnapshot("code-209", "label-257")
        assertEquals(LenientRedeliverySnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLenientRedeliverySnapshot("code-209", "label-257")
        try {
            item.moveTo(LenientRedeliverySnapshotStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRedeliverySnapshotStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientRedeliverySnapshot("code-209", "label-257")
        item.status = LenientRedeliverySnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientRedeliverySnapshotStatus.REQUESTED) }
    }
}
