package com.acme.transport.terminal.sortation.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictSortationManifestTest {
    @Test
    fun `starts in new`() {
        val item = newStrictSortationManifest("code-552", "label-376")
        assertEquals(StrictSortationManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictSortationManifest("code-552", "label-376")
        try {
            item.moveTo(StrictSortationManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictSortationManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictSortationManifest("code-552", "label-376")
        item.status = StrictSortationManifestStatus.FAILED
        assertFails("move") { item.moveTo(StrictSortationManifestStatus.NEW) }
    }
}
