package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCrossDockSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultCrossDockSnapshot("code-367", "name-91")
        assertEquals(DefaultCrossDockSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultCrossDockSnapshot("code-367", "name-91")
        try {
            item.moveTo(DefaultCrossDockSnapshotStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCrossDockSnapshotStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultCrossDockSnapshot("code-367", "name-91")
        item.status = DefaultCrossDockSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultCrossDockSnapshotStatus.REQUESTED) }
    }
}
