package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newSwapAssignment("code-919", "label-624")
        assertEquals(SwapAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSwapAssignment("code-919", "label-624")
        try {
            item.moveTo(SwapAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSwapAssignment("code-919", "label-624")
        item.status = SwapAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(SwapAssignmentStatus.CREATED) }
    }
}
