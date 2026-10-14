package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBackhaulAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newManualBackhaulAllocation("code-656", "name-725")
        assertEquals(ManualBackhaulAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualBackhaulAllocation("code-656", "name-725")
        try {
            item.moveTo(ManualBackhaulAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBackhaulAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualBackhaulAllocation("code-656", "name-725")
        item.status = ManualBackhaulAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualBackhaulAllocationStatus.REQUESTED) }
    }
}
