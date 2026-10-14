package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIdlingScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualIdlingScore("code-802", "name-638")
        assertEquals(ManualIdlingScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualIdlingScore("code-802", "name-638")
        try {
            item.moveTo(ManualIdlingScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIdlingScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualIdlingScore("code-802", "name-638")
        item.status = ManualIdlingScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualIdlingScoreStatus.PROPOSED) }
    }
}
