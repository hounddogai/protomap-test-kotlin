package com.acme.transport.customs.clearance.clearanceestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingClearanceOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newPendingClearanceOverride("code-471", "label-345")
        assertEquals(PendingClearanceOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingClearanceOverride("code-471", "label-345")
        try {
            item.moveTo(PendingClearanceOverrideStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingClearanceOverrideStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingClearanceOverride("code-471", "label-345")
        item.status = PendingClearanceOverrideStatus.FAILED
        assertFails("move") { item.moveTo(PendingClearanceOverrideStatus.NEW) }
    }
}
