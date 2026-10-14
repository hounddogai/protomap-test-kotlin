package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRedeliveryBundleTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackRedeliveryBundle("code-402", "label-601")
        assertEquals(FallbackRedeliveryBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackRedeliveryBundle("code-402", "label-601")
        try {
            item.moveTo(FallbackRedeliveryBundleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRedeliveryBundleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackRedeliveryBundle("code-402", "label-601")
        item.status = FallbackRedeliveryBundleStatus.FAILED
        assertFails("move") { item.moveTo(FallbackRedeliveryBundleStatus.NEW) }
    }
}
