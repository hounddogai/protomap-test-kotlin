package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCapacityBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackCapacityBundle("code-85", "title-721")
        assertEquals(FallbackCapacityBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackCapacityBundle("code-85", "title-721")
        try {
            item.moveTo(FallbackCapacityBundleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCapacityBundleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackCapacityBundle("code-85", "title-721")
        item.status = FallbackCapacityBundleStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackCapacityBundleStatus.DRAFT) }
    }
}
