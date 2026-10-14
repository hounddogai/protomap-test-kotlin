package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newSortationThreshold("code-814", "label-462")
        assertEquals(SortationThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSortationThreshold("code-814", "label-462")
        try {
            item.moveTo(SortationThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSortationThreshold("code-814", "label-462")
        item.status = SortationThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SortationThresholdStatus.PROPOSED) }
    }
}
