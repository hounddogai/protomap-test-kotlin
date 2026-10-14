package com.acme.transport.lastmile.redelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRedeliverySnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalRedeliverySnapshot("code-752", "name-877")
        assertEquals(GlobalRedeliverySnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newGlobalRedeliverySnapshot("code-752", "name-877")
        try {
            item.moveTo(GlobalRedeliverySnapshotStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRedeliverySnapshotStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalRedeliverySnapshot("code-752", "name-877")
        item.status = GlobalRedeliverySnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalRedeliverySnapshotStatus.DRAFT) }
    }
}
