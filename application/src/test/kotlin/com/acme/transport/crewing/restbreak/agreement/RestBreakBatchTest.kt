package com.acme.transport.crewing.restbreak.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakBatchTest {
    @Test
    fun `starts in created`() {
        val item = newRestBreakBatch("code-571", "label-173")
        assertEquals(RestBreakBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRestBreakBatch("code-571", "label-173")
        try {
            item.moveTo(RestBreakBatchStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakBatchStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRestBreakBatch("code-571", "label-173")
        item.status = RestBreakBatchStatus.RETIRED
        assertFails("move") { item.moveTo(RestBreakBatchStatus.CREATED) }
    }
}
