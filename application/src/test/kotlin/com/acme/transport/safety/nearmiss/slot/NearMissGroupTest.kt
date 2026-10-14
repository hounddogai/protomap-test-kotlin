package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newNearMissGroup("code-348", "title-658")
        assertEquals(NearMissGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newNearMissGroup("code-348", "title-658")
        try {
            item.moveTo(NearMissGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newNearMissGroup("code-348", "title-658")
        item.status = NearMissGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(NearMissGroupStatus.REQUESTED) }
    }
}
