package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationManifestTest {
    @Test
    fun `starts in created`() {
        val item = newSortationManifest("code-371", "label-219")
        assertEquals(SortationManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSortationManifest("code-371", "label-219")
        try {
            item.moveTo(SortationManifestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationManifestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSortationManifest("code-371", "label-219")
        item.status = SortationManifestStatus.RETIRED
        assertFails("move") { item.moveTo(SortationManifestStatus.CREATED) }
    }
}
