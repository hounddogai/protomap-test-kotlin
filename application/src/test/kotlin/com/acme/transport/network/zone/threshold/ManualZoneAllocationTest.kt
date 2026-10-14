package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualZoneAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newManualZoneAllocation("code-245", "name-426")
        assertEquals(ManualZoneAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualZoneAllocation("code-245", "name-426")
        try {
            item.moveTo(ManualZoneAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualZoneAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualZoneAllocation("code-245", "name-426")
        item.status = ManualZoneAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(ManualZoneAllocationStatus.CREATED) }
    }
}
