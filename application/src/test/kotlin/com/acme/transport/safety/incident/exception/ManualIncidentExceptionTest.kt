package com.acme.transport.safety.incident.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIncidentExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newManualIncidentException("code-892", "title-993")
        assertEquals(ManualIncidentExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualIncidentException("code-892", "title-993")
        try {
            item.moveTo(ManualIncidentExceptionStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIncidentExceptionStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualIncidentException("code-892", "title-993")
        item.status = ManualIncidentExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(ManualIncidentExceptionStatus.CREATED) }
    }
}
