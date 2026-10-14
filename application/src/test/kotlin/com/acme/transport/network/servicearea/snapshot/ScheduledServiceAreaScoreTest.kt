package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledServiceAreaScoreTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledServiceAreaScore("code-233", "name-301")
        assertEquals(ScheduledServiceAreaScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledServiceAreaScore("code-233", "name-301")
        try {
            item.moveTo(ScheduledServiceAreaScoreStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledServiceAreaScoreStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledServiceAreaScore("code-233", "name-301")
        item.status = ScheduledServiceAreaScoreStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledServiceAreaScoreStatus.NEW) }
    }
}
