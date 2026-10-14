package com.acme.transport.customs.clearance.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyClearanceGroupTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyClearanceGroup("code-988", "label-891")
        assertEquals(LegacyClearanceGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyClearanceGroup("code-988", "label-891")
        try {
            item.moveTo(LegacyClearanceGroupStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyClearanceGroupStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyClearanceGroup("code-988", "label-891")
        item.status = LegacyClearanceGroupStatus.FAILED
        assertFails("move") { item.moveTo(LegacyClearanceGroupStatus.NEW) }
    }
}
