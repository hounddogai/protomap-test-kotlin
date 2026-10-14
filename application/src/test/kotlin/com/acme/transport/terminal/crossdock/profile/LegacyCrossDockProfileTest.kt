package com.acme.transport.terminal.crossdock.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCrossDockProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyCrossDockProfile("code-329", "label-330")
        assertEquals(LegacyCrossDockProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLegacyCrossDockProfile("code-329", "label-330")
        try {
            item.moveTo(LegacyCrossDockProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCrossDockProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyCrossDockProfile("code-329", "label-330")
        item.status = LegacyCrossDockProfileStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyCrossDockProfileStatus.DRAFT) }
    }
}
