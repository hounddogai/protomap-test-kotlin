package com.acme.transport.terminal.sortation.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSortationDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredSortationDigest("code-135", "title-661")
        assertEquals(TieredSortationDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredSortationDigest("code-135", "title-661")
        try {
            item.moveTo(TieredSortationDigestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSortationDigestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredSortationDigest("code-135", "title-661")
        item.status = TieredSortationDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredSortationDigestStatus.REQUESTED) }
    }
}
