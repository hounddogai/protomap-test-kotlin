package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newIncidentBundle("code-7", "name-362")
        assertEquals(IncidentBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIncidentBundle("code-7", "name-362")
        try {
            item.moveTo(IncidentBundleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentBundleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIncidentBundle("code-7", "name-362")
        item.status = IncidentBundleStatus.CLOSED
        assertFails("move") { item.moveTo(IncidentBundleStatus.DRAFT) }
    }
}
