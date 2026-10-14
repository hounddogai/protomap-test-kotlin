package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newSortationOverride("code-775", "label-821")
        assertEquals(SortationOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSortationOverride("code-775", "label-821")
        try {
            item.moveTo(SortationOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSortationOverride("code-775", "label-821")
        item.status = SortationOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(SortationOverrideStatus.CREATED) }
    }
}
