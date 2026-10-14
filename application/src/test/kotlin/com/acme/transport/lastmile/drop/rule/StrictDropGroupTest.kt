package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDropGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictDropGroup("code-352", "title-126")
        assertEquals(StrictDropGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newStrictDropGroup("code-352", "title-126")
        try {
            item.moveTo(StrictDropGroupStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDropGroupStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictDropGroup("code-352", "title-126")
        item.status = StrictDropGroupStatus.CLOSED
        assertFails("move") { item.moveTo(StrictDropGroupStatus.DRAFT) }
    }
}
