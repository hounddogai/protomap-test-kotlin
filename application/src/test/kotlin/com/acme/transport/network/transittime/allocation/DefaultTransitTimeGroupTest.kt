package com.acme.transport.network.transittime.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTransitTimeGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultTransitTimeGroup("code-327", "label-581")
        assertEquals(DefaultTransitTimeGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDefaultTransitTimeGroup("code-327", "label-581")
        try {
            item.moveTo(DefaultTransitTimeGroupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTransitTimeGroupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultTransitTimeGroup("code-327", "label-581")
        item.status = DefaultTransitTimeGroupStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultTransitTimeGroupStatus.DRAFT) }
    }
}
