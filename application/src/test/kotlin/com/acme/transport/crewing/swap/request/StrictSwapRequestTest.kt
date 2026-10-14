package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictSwapRequestTest {
    @Test
    fun `starts in open`() {
        val item = newStrictSwapRequest("code-154", "title-807")
        assertEquals(StrictSwapRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictSwapRequest("code-154", "title-807")
        try {
            item.moveTo(StrictSwapRequestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictSwapRequestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictSwapRequest("code-154", "title-807")
        item.status = StrictSwapRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictSwapRequestStatus.OPEN) }
    }
}
