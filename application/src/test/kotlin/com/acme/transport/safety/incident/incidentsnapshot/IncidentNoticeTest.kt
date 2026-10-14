package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newIncidentNotice("code-921", "name-366")
        assertEquals(IncidentNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newIncidentNotice("code-921", "name-366")
        try {
            item.moveTo(IncidentNoticeStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentNoticeStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newIncidentNotice("code-921", "name-366")
        item.status = IncidentNoticeStatus.FAILED
        assertFails("move") { item.moveTo(IncidentNoticeStatus.NEW) }
    }
}
