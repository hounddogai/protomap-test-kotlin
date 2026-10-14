package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackNodeAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackNodeAdjustment("code-970", "label-455")
        assertEquals(FallbackNodeAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackNodeAdjustment("code-970", "label-455")
        try {
            item.moveTo(FallbackNodeAdjustmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackNodeAdjustmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackNodeAdjustment("code-970", "label-455")
        item.status = FallbackNodeAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackNodeAdjustmentStatus.REQUESTED) }
    }
}
