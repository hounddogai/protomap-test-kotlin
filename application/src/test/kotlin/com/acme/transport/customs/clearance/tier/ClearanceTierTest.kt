package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceTierTest {
    @Test
    fun `starts in requested`() {
        val item = newClearanceTier("code-544", "label-106")
        assertEquals(ClearanceTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newClearanceTier("code-544", "label-106")
        try {
            item.moveTo(ClearanceTierStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceTierStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newClearanceTier("code-544", "label-106")
        item.status = ClearanceTierStatus.EXPIRED
        assertFails("move") { item.moveTo(ClearanceTierStatus.REQUESTED) }
    }
}
