package com.acme.transport.customs.clearance.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceRollupTest {
    @Test
    fun `starts in new`() {
        val item = newClearanceRollup("code-461", "name-357")
        assertEquals(ClearanceRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newClearanceRollup("code-461", "name-357")
        try {
            item.moveTo(ClearanceRollupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceRollupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newClearanceRollup("code-461", "name-357")
        item.status = ClearanceRollupStatus.FAILED
        assertFails("move") { item.moveTo(ClearanceRollupStatus.NEW) }
    }
}
