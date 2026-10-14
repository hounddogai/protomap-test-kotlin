package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRestBreakExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newTieredRestBreakException("code-378", "label-763")
        assertEquals(TieredRestBreakExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTieredRestBreakException("code-378", "label-763")
        try {
            item.moveTo(TieredRestBreakExceptionStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRestBreakExceptionStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredRestBreakException("code-378", "label-763")
        item.status = TieredRestBreakExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(TieredRestBreakExceptionStatus.CREATED) }
    }
}
