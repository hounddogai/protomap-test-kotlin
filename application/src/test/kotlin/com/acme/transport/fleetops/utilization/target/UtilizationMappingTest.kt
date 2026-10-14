package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newUtilizationMapping("code-210", "label-484")
        assertEquals(UtilizationMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newUtilizationMapping("code-210", "label-484")
        try {
            item.moveTo(UtilizationMappingStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationMappingStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newUtilizationMapping("code-210", "label-484")
        item.status = UtilizationMappingStatus.CLOSED
        assertFails("move") { item.moveTo(UtilizationMappingStatus.DRAFT) }
    }
}
