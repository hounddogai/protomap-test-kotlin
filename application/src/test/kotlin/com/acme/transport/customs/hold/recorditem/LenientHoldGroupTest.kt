package com.acme.transport.customs.hold.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientHoldGroupTest {
    @Test
    fun `starts in created`() {
        val item = newLenientHoldGroup("code-421", "label-32")
        assertEquals(LenientHoldGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientHoldGroup("code-421", "label-32")
        try {
            item.moveTo(LenientHoldGroupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientHoldGroupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientHoldGroup("code-421", "label-32")
        item.status = LenientHoldGroupStatus.RETIRED
        assertFails("move") { item.moveTo(LenientHoldGroupStatus.CREATED) }
    }
}
