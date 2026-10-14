package com.acme.transport.network.node.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNodeProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryNodeProfile("code-330", "name-855")
        assertEquals(PrimaryNodeProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newPrimaryNodeProfile("code-330", "name-855")
        try {
            item.moveTo(PrimaryNodeProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNodeProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryNodeProfile("code-330", "name-855")
        item.status = PrimaryNodeProfileStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryNodeProfileStatus.DRAFT) }
    }
}
