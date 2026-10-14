package com.acme.transport.lastmile.drop.dropdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newDropOverride("code-584", "label-468")
        assertEquals(DropOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDropOverride("code-584", "label-468")
        try {
            item.moveTo(DropOverrideStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropOverrideStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDropOverride("code-584", "label-468")
        item.status = DropOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DropOverrideStatus.PROPOSED) }
    }
}
