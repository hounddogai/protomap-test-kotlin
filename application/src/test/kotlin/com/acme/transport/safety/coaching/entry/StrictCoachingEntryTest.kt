package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCoachingEntryTest {
    @Test
    fun `starts in created`() {
        val item = newStrictCoachingEntry("code-67", "name-480")
        assertEquals(StrictCoachingEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictCoachingEntry("code-67", "name-480")
        try {
            item.moveTo(StrictCoachingEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCoachingEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictCoachingEntry("code-67", "name-480")
        item.status = StrictCoachingEntryStatus.RETIRED
        assertFails("move") { item.moveTo(StrictCoachingEntryStatus.CREATED) }
    }
}
