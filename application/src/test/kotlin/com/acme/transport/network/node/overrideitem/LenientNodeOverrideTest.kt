package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNodeOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newLenientNodeOverride("code-878", "name-519")
        assertEquals(LenientNodeOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLenientNodeOverride("code-878", "name-519")
        try {
            item.moveTo(LenientNodeOverrideStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNodeOverrideStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientNodeOverride("code-878", "name-519")
        item.status = LenientNodeOverrideStatus.FAILED
        assertFails("move") { item.moveTo(LenientNodeOverrideStatus.NEW) }
    }
}
