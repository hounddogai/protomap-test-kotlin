package com.acme.transport.terminal.sortation.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newSortationScore("code-121", "label-500")
        assertEquals(SortationScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSortationScore("code-121", "label-500")
        try {
            item.moveTo(SortationScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSortationScore("code-121", "label-500")
        item.status = SortationScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SortationScoreStatus.PROPOSED) }
    }
}
