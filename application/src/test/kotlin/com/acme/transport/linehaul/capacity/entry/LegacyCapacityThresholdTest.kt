package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCapacityThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyCapacityThreshold("code-139", "name-558")
        assertEquals(LegacyCapacityThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyCapacityThreshold("code-139", "name-558")
        try {
            item.moveTo(LegacyCapacityThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCapacityThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyCapacityThreshold("code-139", "name-558")
        item.status = LegacyCapacityThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyCapacityThresholdStatus.PROPOSED) }
    }
}
