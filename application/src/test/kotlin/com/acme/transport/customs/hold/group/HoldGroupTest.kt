package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldGroupTest {
    @Test
    fun `starts in open`() {
        val item = newHoldGroup("code-765", "name-675")
        assertEquals(HoldGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newHoldGroup("code-765", "name-675")
        try {
            item.moveTo(HoldGroupStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldGroupStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newHoldGroup("code-765", "name-675")
        item.status = HoldGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(HoldGroupStatus.OPEN) }
    }
}
