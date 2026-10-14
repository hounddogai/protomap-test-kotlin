package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveZoneAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveZoneAllocation("code-582", "name-290")
        assertEquals(AdaptiveZoneAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveZoneAllocation("code-582", "name-290")
        try {
            item.moveTo(AdaptiveZoneAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveZoneAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveZoneAllocation("code-582", "name-290")
        item.status = AdaptiveZoneAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveZoneAllocationStatus.CREATED) }
    }
}
