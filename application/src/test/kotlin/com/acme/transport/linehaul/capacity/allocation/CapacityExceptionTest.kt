package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newCapacityException("code-840", "name-898")
        assertEquals(CapacityExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCapacityException("code-840", "name-898")
        try {
            item.moveTo(CapacityExceptionStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityExceptionStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCapacityException("code-840", "name-898")
        item.status = CapacityExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(CapacityExceptionStatus.CREATED) }
    }
}
