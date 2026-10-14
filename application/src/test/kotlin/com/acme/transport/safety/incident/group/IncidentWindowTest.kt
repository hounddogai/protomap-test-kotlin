package com.acme.transport.safety.incident.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newIncidentWindow("code-207", "label-302")
        assertEquals(IncidentWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newIncidentWindow("code-207", "label-302")
        try {
            item.moveTo(IncidentWindowStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentWindowStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newIncidentWindow("code-207", "label-302")
        item.status = IncidentWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(IncidentWindowStatus.REQUESTED) }
    }
}
