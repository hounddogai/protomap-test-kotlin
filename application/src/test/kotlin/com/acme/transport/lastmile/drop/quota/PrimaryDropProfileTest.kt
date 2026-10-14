package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDropProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryDropProfile("code-947", "name-10")
        assertEquals(PrimaryDropProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryDropProfile("code-947", "name-10")
        try {
            item.moveTo(PrimaryDropProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDropProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryDropProfile("code-947", "name-10")
        item.status = PrimaryDropProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryDropProfileStatus.REQUESTED) }
    }
}
