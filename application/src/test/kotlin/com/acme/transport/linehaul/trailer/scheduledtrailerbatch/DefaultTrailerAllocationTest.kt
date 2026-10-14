package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTrailerAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultTrailerAllocation("code-635", "name-642")
        assertEquals(DefaultTrailerAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultTrailerAllocation("code-635", "name-642")
        try {
            item.moveTo(DefaultTrailerAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTrailerAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultTrailerAllocation("code-635", "name-642")
        item.status = DefaultTrailerAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultTrailerAllocationStatus.IDLE) }
    }
}
