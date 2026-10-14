package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalUtilizationMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalUtilizationMapping("code-255", "name-216")
        assertEquals(GlobalUtilizationMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalUtilizationMapping("code-255", "name-216")
        try {
            item.moveTo(GlobalUtilizationMappingStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalUtilizationMappingStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalUtilizationMapping("code-255", "name-216")
        item.status = GlobalUtilizationMappingStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalUtilizationMappingStatus.IDLE) }
    }
}
