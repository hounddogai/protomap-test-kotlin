package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRestBreakManifestTest {
    @Test
    fun `starts in new`() {
        val item = newTieredRestBreakManifest("code-43", "name-977")
        assertEquals(TieredRestBreakManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTieredRestBreakManifest("code-43", "name-977")
        try {
            item.moveTo(TieredRestBreakManifestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRestBreakManifestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredRestBreakManifest("code-43", "name-977")
        item.status = TieredRestBreakManifestStatus.FAILED
        assertFails("move") { item.moveTo(TieredRestBreakManifestStatus.NEW) }
    }
}
