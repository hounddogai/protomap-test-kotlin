package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredNodeBundleTest {
    @Test
    fun `starts in open`() {
        val item = newTieredNodeBundle("code-46", "name-41")
        assertEquals(TieredNodeBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTieredNodeBundle("code-46", "name-41")
        try {
            item.moveTo(TieredNodeBundleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredNodeBundleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredNodeBundle("code-46", "name-41")
        item.status = TieredNodeBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredNodeBundleStatus.OPEN) }
    }
}
