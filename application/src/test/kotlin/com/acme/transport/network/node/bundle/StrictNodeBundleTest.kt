package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictNodeBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictNodeBundle("code-30", "label-459")
        assertEquals(StrictNodeBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newStrictNodeBundle("code-30", "label-459")
        try {
            item.moveTo(StrictNodeBundleStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictNodeBundleStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictNodeBundle("code-30", "label-459")
        item.status = StrictNodeBundleStatus.CLOSED
        assertFails("move") { item.moveTo(StrictNodeBundleStatus.DRAFT) }
    }
}
