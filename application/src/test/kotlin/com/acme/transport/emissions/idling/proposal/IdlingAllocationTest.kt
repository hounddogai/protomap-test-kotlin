package com.acme.transport.emissions.idling.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newIdlingAllocation("code-663", "label-747")
        assertEquals(IdlingAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newIdlingAllocation("code-663", "label-747")
        try {
            item.moveTo(IdlingAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newIdlingAllocation("code-663", "label-747")
        item.status = IdlingAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(IdlingAllocationStatus.REQUESTED) }
    }
}
