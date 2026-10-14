package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingSegment("code-454", "name-83")
        assertEquals(IdlingSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIdlingSegment("code-454", "name-83")
        try {
            item.moveTo(IdlingSegmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingSegmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingSegment("code-454", "name-83")
        item.status = IdlingSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingSegmentStatus.CREATED) }
    }
}
