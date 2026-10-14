package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newEvChargeAllocation("code-265", "name-664")
        assertEquals(EvChargeAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newEvChargeAllocation("code-265", "name-664")
        try {
            item.moveTo(EvChargeAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newEvChargeAllocation("code-265", "name-664")
        item.status = EvChargeAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(EvChargeAllocationStatus.PROPOSED) }
    }
}
