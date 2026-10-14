package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newIncidentScore("code-897", "title-580")
        assertEquals(IncidentScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newIncidentScore("code-897", "title-580")
        try {
            item.moveTo(IncidentScoreStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentScoreStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIncidentScore("code-897", "title-580")
        item.status = IncidentScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IncidentScoreStatus.PROPOSED) }
    }
}
