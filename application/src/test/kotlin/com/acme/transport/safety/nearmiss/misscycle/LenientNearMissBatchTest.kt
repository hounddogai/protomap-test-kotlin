package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNearMissBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientNearMissBatch("code-518", "title-324")
        assertEquals(LenientNearMissBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientNearMissBatch("code-518", "title-324")
        try {
            item.moveTo(LenientNearMissBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNearMissBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientNearMissBatch("code-518", "title-324")
        item.status = LenientNearMissBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientNearMissBatchStatus.PROPOSED) }
    }
}
