package com.acme.transport.emissions.idling.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingIdlingSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingIdlingSegment("code-190", "name-834")
        assertEquals(RollingIdlingSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingIdlingSegment("code-190", "name-834")
        try {
            item.moveTo(RollingIdlingSegmentStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingIdlingSegmentStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingIdlingSegment("code-190", "name-834")
        item.status = RollingIdlingSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(RollingIdlingSegmentStatus.DRAFT) }
    }
}
