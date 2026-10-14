package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newCarbonReportAllocation("code-388", "name-121")
        assertEquals(CarbonReportAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCarbonReportAllocation("code-388", "name-121")
        try {
            item.moveTo(CarbonReportAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCarbonReportAllocation("code-388", "name-121")
        item.status = CarbonReportAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(CarbonReportAllocationStatus.IDLE) }
    }
}
