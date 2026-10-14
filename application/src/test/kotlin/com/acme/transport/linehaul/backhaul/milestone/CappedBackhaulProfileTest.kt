package com.acme.transport.linehaul.backhaul.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBackhaulProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedBackhaulProfile("code-502", "name-796")
        assertEquals(CappedBackhaulProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCappedBackhaulProfile("code-502", "name-796")
        try {
            item.moveTo(CappedBackhaulProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBackhaulProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedBackhaulProfile("code-502", "name-796")
        item.status = CappedBackhaulProfileStatus.CLOSED
        assertFails("move") { item.moveTo(CappedBackhaulProfileStatus.DRAFT) }
    }
}
