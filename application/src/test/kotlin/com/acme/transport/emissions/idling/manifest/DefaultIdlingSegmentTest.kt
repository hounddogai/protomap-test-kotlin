package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultIdlingSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultIdlingSegment("code-469", "title-458")
        assertEquals(DefaultIdlingSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultIdlingSegment("code-469", "title-458")
        try {
            item.moveTo(DefaultIdlingSegmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultIdlingSegmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultIdlingSegment("code-469", "title-458")
        item.status = DefaultIdlingSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultIdlingSegmentStatus.CREATED) }
    }
}
