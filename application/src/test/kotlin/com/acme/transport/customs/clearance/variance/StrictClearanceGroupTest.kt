package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictClearanceGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictClearanceGroup("code-214", "label-433")
        assertEquals(StrictClearanceGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictClearanceGroup("code-214", "label-433")
        try {
            item.moveTo(StrictClearanceGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictClearanceGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictClearanceGroup("code-214", "label-433")
        item.status = StrictClearanceGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictClearanceGroupStatus.REQUESTED) }
    }
}
