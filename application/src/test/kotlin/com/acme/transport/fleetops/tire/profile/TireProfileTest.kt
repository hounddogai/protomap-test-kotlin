package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newTireProfile("code-671", "name-614")
        assertEquals(TireProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTireProfile("code-671", "name-614")
        try {
            item.moveTo(TireProfileStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireProfileStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTireProfile("code-671", "name-614")
        item.status = TireProfileStatus.CLOSED
        assertFails("move") { item.moveTo(TireProfileStatus.DRAFT) }
    }
}
