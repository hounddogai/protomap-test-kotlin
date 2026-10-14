package com.acme.transport.fleetops.defect.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDefectAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalDefectAllocation("code-296", "name-418")
        assertEquals(RegionalDefectAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalDefectAllocation("code-296", "name-418")
        try {
            item.moveTo(RegionalDefectAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDefectAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalDefectAllocation("code-296", "name-418")
        item.status = RegionalDefectAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalDefectAllocationStatus.PROPOSED) }
    }
}
