package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newIdlingSnapshot("code-222", "title-603")
        assertEquals(IdlingSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newIdlingSnapshot("code-222", "title-603")
        try {
            item.moveTo(IdlingSnapshotStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingSnapshotStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newIdlingSnapshot("code-222", "title-603")
        item.status = IdlingSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(IdlingSnapshotStatus.REQUESTED) }
    }
}
