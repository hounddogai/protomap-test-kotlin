package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newBackhaulAllocation("code-624", "label-124")
        assertEquals(BackhaulAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBackhaulAllocation("code-624", "label-124")
        try {
            item.moveTo(BackhaulAllocationStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulAllocationStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBackhaulAllocation("code-624", "label-124")
        item.status = BackhaulAllocationStatus.FAILED
        assertFails("move") { item.moveTo(BackhaulAllocationStatus.NEW) }
    }
}
