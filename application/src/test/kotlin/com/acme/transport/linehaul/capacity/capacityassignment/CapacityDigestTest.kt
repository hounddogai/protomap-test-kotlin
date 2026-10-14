package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacityDigest("code-431", "title-776")
        assertEquals(CapacityDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCapacityDigest("code-431", "title-776")
        try {
            item.moveTo(CapacityDigestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityDigestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacityDigest("code-431", "title-776")
        item.status = CapacityDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacityDigestStatus.REQUESTED) }
    }
}
