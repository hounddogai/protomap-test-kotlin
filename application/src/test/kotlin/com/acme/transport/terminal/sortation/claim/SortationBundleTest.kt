package com.acme.transport.terminal.sortation.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationBundleTest {
    @Test
    fun `starts in open`() {
        val item = newSortationBundle("code-135", "title-315")
        assertEquals(SortationBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newSortationBundle("code-135", "title-315")
        try {
            item.moveTo(SortationBundleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationBundleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSortationBundle("code-135", "title-315")
        item.status = SortationBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(SortationBundleStatus.OPEN) }
    }
}
