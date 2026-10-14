package com.acme.transport.safety.coaching.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCoachingRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalCoachingRequest("code-109", "title-724")
        assertEquals(SeasonalCoachingRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newSeasonalCoachingRequest("code-109", "title-724")
        try {
            item.moveTo(SeasonalCoachingRequestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCoachingRequestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalCoachingRequest("code-109", "title-724")
        item.status = SeasonalCoachingRequestStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalCoachingRequestStatus.DRAFT) }
    }
}
