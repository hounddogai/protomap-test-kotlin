package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAccessorialAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultAccessorialAllocation("code-355", "label-174")
        assertEquals(DefaultAccessorialAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultAccessorialAllocation("code-355", "label-174")
        try {
            item.moveTo(DefaultAccessorialAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAccessorialAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultAccessorialAllocation("code-355", "label-174")
        item.status = DefaultAccessorialAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultAccessorialAllocationStatus.PROPOSED) }
    }
}
