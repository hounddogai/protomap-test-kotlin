package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newIncidentException("code-986", "label-16")
        assertEquals(IncidentExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newIncidentException("code-986", "label-16")
        try {
            item.moveTo(IncidentExceptionStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentExceptionStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIncidentException("code-986", "label-16")
        item.status = IncidentExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(IncidentExceptionStatus.CREATED) }
    }
}
