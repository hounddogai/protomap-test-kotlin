package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRosterRequestTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackRosterRequest("code-811", "name-212")
        assertEquals(FallbackRosterRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackRosterRequest("code-811", "name-212")
        try {
            item.moveTo(FallbackRosterRequestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRosterRequestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackRosterRequest("code-811", "name-212")
        item.status = FallbackRosterRequestStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackRosterRequestStatus.CREATED) }
    }
}
