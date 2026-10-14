package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRedeliverySnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackRedeliverySnapshot("code-230", "name-354")
        assertEquals(FallbackRedeliverySnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackRedeliverySnapshot("code-230", "name-354")
        try {
            item.moveTo(FallbackRedeliverySnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRedeliverySnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackRedeliverySnapshot("code-230", "name-354")
        item.status = FallbackRedeliverySnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackRedeliverySnapshotStatus.PROPOSED) }
    }
}
