package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNodeBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingNodeBundle("code-849", "name-751")
        assertEquals(PendingNodeBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingNodeBundle("code-849", "name-751")
        try {
            item.moveTo(PendingNodeBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNodeBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingNodeBundle("code-849", "name-751")
        item.status = PendingNodeBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingNodeBundleStatus.REQUESTED) }
    }
}
