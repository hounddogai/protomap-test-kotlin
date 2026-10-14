package com.acme.transport.network.node.nodeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNodeOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveNodeOverride("code-934", "title-399")
        assertEquals(AdaptiveNodeOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveNodeOverride("code-934", "title-399")
        try {
            item.moveTo(AdaptiveNodeOverrideStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNodeOverrideStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveNodeOverride("code-934", "title-399")
        item.status = AdaptiveNodeOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveNodeOverrideStatus.DRAFT) }
    }
}
