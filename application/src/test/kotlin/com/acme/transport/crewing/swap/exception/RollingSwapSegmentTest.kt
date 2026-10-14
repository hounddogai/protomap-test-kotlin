package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingSwapSegment("code-534", "name-829")
        assertEquals(RollingSwapSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRollingSwapSegment("code-534", "name-829")
        try {
            item.moveTo(RollingSwapSegmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapSegmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingSwapSegment("code-534", "name-829")
        item.status = RollingSwapSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingSwapSegmentStatus.REQUESTED) }
    }
}
