package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIncidentExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalIncidentException("code-369", "name-529")
        assertEquals(RegionalIncidentExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRegionalIncidentException("code-369", "name-529")
        try {
            item.moveTo(RegionalIncidentExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIncidentExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalIncidentException("code-369", "name-529")
        item.status = RegionalIncidentExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalIncidentExceptionStatus.REQUESTED) }
    }
}
