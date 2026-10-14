package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newTireMapping("code-197", "name-223")
        assertEquals(TireMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTireMapping("code-197", "name-223")
        try {
            item.moveTo(TireMappingStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireMappingStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTireMapping("code-197", "name-223")
        item.status = TireMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(TireMappingStatus.PLANNED) }
    }
}
