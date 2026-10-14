package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newDwellOverride("code-536", "title-333")
        assertEquals(DwellOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDwellOverride("code-536", "title-333")
        try {
            item.moveTo(DwellOverrideStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellOverrideStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDwellOverride("code-536", "title-333")
        item.status = DwellOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(DwellOverrideStatus.DRAFT) }
    }
}
