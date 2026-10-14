package com.acme.transport.network.servicearea.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictServiceAreaBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictServiceAreaBatch("code-187", "label-983")
        assertEquals(StrictServiceAreaBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictServiceAreaBatch("code-187", "label-983")
        try {
            item.moveTo(StrictServiceAreaBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictServiceAreaBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictServiceAreaBatch("code-187", "label-983")
        item.status = StrictServiceAreaBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictServiceAreaBatchStatus.PROPOSED) }
    }
}
