package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackIncidentWindowTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackIncidentWindow("code-539", "label-741")
        assertEquals(FallbackIncidentWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackIncidentWindow("code-539", "label-741")
        try {
            item.moveTo(FallbackIncidentWindowStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackIncidentWindowStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackIncidentWindow("code-539", "label-741")
        item.status = FallbackIncidentWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackIncidentWindowStatus.OPEN) }
    }
}
