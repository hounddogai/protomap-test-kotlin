package com.acme.transport.customs.hold.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedHoldRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedHoldRollup("code-69", "title-449")
        assertEquals(CappedHoldRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCappedHoldRollup("code-69", "title-449")
        try {
            item.moveTo(CappedHoldRollupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedHoldRollupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedHoldRollup("code-69", "title-449")
        item.status = CappedHoldRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedHoldRollupStatus.REQUESTED) }
    }
}
