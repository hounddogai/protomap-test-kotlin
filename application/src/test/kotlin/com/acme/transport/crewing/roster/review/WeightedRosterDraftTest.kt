package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRosterDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedRosterDraft("code-157", "label-400")
        assertEquals(WeightedRosterDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedRosterDraft("code-157", "label-400")
        try {
            item.moveTo(WeightedRosterDraftStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRosterDraftStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedRosterDraft("code-157", "label-400")
        item.status = WeightedRosterDraftStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedRosterDraftStatus.DRAFT) }
    }
}
