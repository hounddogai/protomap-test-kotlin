package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRelayBundleTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalRelayBundle("code-388", "label-712")
        assertEquals(GlobalRelayBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newGlobalRelayBundle("code-388", "label-712")
        try {
            item.moveTo(GlobalRelayBundleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRelayBundleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalRelayBundle("code-388", "label-712")
        item.status = GlobalRelayBundleStatus.FAILED
        assertFails("move") { item.moveTo(GlobalRelayBundleStatus.NEW) }
    }
}
