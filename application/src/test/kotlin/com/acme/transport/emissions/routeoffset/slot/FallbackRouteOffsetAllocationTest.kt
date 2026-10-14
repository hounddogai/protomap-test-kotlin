package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackRouteOffsetAllocation("code-989", "title-854")
        assertEquals(FallbackRouteOffsetAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFallbackRouteOffsetAllocation("code-989", "title-854")
        try {
            item.moveTo(FallbackRouteOffsetAllocationStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetAllocationStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackRouteOffsetAllocation("code-989", "title-854")
        item.status = FallbackRouteOffsetAllocationStatus.FAILED
        assertFails("move") { item.moveTo(FallbackRouteOffsetAllocationStatus.NEW) }
    }
}
