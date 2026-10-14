package com.acme.transport.safety.incident.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentGroupTest {
    @Test
    fun `starts in open`() {
        val item = newIncidentGroup("code-647", "title-500")
        assertEquals(IncidentGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newIncidentGroup("code-647", "title-500")
        try {
            item.moveTo(IncidentGroupStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentGroupStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newIncidentGroup("code-647", "title-500")
        item.status = IncidentGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(IncidentGroupStatus.OPEN) }
    }
}
