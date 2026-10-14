package com.acme.transport.safety.incident.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultIncidentCycleTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultIncidentCycle("code-245", "name-99")
        assertEquals(DefaultIncidentCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultIncidentCycle("code-245", "name-99")
        try {
            item.moveTo(DefaultIncidentCycleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultIncidentCycleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultIncidentCycle("code-245", "name-99")
        item.status = DefaultIncidentCycleStatus.FAILED
        assertFails("move") { item.moveTo(DefaultIncidentCycleStatus.NEW) }
    }
}
