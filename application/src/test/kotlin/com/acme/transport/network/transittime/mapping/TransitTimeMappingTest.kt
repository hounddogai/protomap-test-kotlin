package com.acme.transport.network.transittime.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newTransitTimeMapping("code-361", "title-886")
        assertEquals(TransitTimeMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTransitTimeMapping("code-361", "title-886")
        try {
            item.moveTo(TransitTimeMappingStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeMappingStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTransitTimeMapping("code-361", "title-886")
        item.status = TransitTimeMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(TransitTimeMappingStatus.PLANNED) }
    }
}
