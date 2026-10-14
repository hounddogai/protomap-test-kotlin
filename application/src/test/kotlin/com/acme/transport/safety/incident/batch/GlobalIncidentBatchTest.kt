package com.acme.transport.safety.incident.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalIncidentBatchTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalIncidentBatch("code-151", "title-561")
        assertEquals(GlobalIncidentBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newGlobalIncidentBatch("code-151", "title-561")
        try {
            item.moveTo(GlobalIncidentBatchStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalIncidentBatchStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalIncidentBatch("code-151", "title-561")
        item.status = GlobalIncidentBatchStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalIncidentBatchStatus.CREATED) }
    }
}
