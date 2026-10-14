package com.acme.transport.safety.incident.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newIncidentBatch("code-358", "label-894")
        assertEquals(IncidentBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newIncidentBatch("code-358", "label-894")
        try {
            item.moveTo(IncidentBatchStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentBatchStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newIncidentBatch("code-358", "label-894")
        item.status = IncidentBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(IncidentBatchStatus.REQUESTED) }
    }
}
