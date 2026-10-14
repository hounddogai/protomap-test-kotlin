package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newDropBaseline("code-685", "label-774")
        assertEquals(DropBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDropBaseline("code-685", "label-774")
        try {
            item.moveTo(DropBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDropBaseline("code-685", "label-774")
        item.status = DropBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(DropBaselineStatus.REQUESTED) }
    }
}
