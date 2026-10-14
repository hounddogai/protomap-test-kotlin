package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackIdlingSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackIdlingSegment("code-743", "title-233")
        assertEquals(FallbackIdlingSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFallbackIdlingSegment("code-743", "title-233")
        try {
            item.moveTo(FallbackIdlingSegmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackIdlingSegmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackIdlingSegment("code-743", "title-233")
        item.status = FallbackIdlingSegmentStatus.FAILED
        assertFails("move") { item.moveTo(FallbackIdlingSegmentStatus.NEW) }
    }
}
