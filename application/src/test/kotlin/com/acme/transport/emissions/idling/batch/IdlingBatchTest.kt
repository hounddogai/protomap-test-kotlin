package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newIdlingBatch("code-820", "title-318")
        assertEquals(IdlingBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIdlingBatch("code-820", "title-318")
        try {
            item.moveTo(IdlingBatchStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingBatchStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIdlingBatch("code-820", "title-318")
        item.status = IdlingBatchStatus.CLOSED
        assertFails("move") { item.moveTo(IdlingBatchStatus.DRAFT) }
    }
}
