package com.acme.transport.lastmile.drop.cappeddropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newDropGroup("code-168", "name-996")
        assertEquals(DropGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDropGroup("code-168", "name-996")
        try {
            item.moveTo(DropGroupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropGroupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDropGroup("code-168", "name-996")
        item.status = DropGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DropGroupStatus.PROPOSED) }
    }
}
