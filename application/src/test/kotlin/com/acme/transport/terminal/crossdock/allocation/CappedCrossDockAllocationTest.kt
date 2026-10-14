package com.acme.transport.terminal.crossdock.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCrossDockAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newCappedCrossDockAllocation("code-889", "label-382")
        assertEquals(CappedCrossDockAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newCappedCrossDockAllocation("code-889", "label-382")
        try {
            item.moveTo(CappedCrossDockAllocationStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCrossDockAllocationStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedCrossDockAllocation("code-889", "label-382")
        item.status = CappedCrossDockAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(CappedCrossDockAllocationStatus.CREATED) }
    }
}
