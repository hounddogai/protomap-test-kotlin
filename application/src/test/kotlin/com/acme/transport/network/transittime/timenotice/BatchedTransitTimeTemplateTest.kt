package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTransitTimeTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedTransitTimeTemplate("code-420", "name-192")
        assertEquals(BatchedTransitTimeTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newBatchedTransitTimeTemplate("code-420", "name-192")
        try {
            item.moveTo(BatchedTransitTimeTemplateStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTransitTimeTemplateStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedTransitTimeTemplate("code-420", "name-192")
        item.status = BatchedTransitTimeTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedTransitTimeTemplateStatus.IDLE) }
    }
}
