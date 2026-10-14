package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newClearanceScore("code-785", "label-165")
        assertEquals(ClearanceScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newClearanceScore("code-785", "label-165")
        try {
            item.moveTo(ClearanceScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newClearanceScore("code-785", "label-165")
        item.status = ClearanceScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ClearanceScoreStatus.PROPOSED) }
    }
}
