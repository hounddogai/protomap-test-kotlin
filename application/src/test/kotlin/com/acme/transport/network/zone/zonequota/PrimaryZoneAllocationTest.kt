package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryZoneAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryZoneAllocation("code-348", "title-448")
        assertEquals(PrimaryZoneAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryZoneAllocation("code-348", "title-448")
        try {
            item.moveTo(PrimaryZoneAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryZoneAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryZoneAllocation("code-348", "title-448")
        item.status = PrimaryZoneAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryZoneAllocationStatus.PROPOSED) }
    }
}
