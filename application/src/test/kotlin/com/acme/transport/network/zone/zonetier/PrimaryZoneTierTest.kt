package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryZoneTierTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryZoneTier("code-220", "title-124")
        assertEquals(PrimaryZoneTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPrimaryZoneTier("code-220", "title-124")
        try {
            item.moveTo(PrimaryZoneTierStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryZoneTierStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryZoneTier("code-220", "title-124")
        item.status = PrimaryZoneTierStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryZoneTierStatus.DRAFT) }
    }
}
