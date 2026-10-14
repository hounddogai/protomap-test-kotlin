package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCapacityExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultCapacityException("code-368", "name-930")
        assertEquals(DefaultCapacityExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultCapacityException("code-368", "name-930")
        try {
            item.moveTo(DefaultCapacityExceptionStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCapacityExceptionStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultCapacityException("code-368", "name-930")
        item.status = DefaultCapacityExceptionStatus.FAILED
        assertFails("move") { item.moveTo(DefaultCapacityExceptionStatus.NEW) }
    }
}
