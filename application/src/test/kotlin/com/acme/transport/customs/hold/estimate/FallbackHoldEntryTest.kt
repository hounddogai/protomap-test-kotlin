package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackHoldEntryTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackHoldEntry("code-910", "title-247")
        assertEquals(FallbackHoldEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newFallbackHoldEntry("code-910", "title-247")
        try {
            item.moveTo(FallbackHoldEntryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackHoldEntryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackHoldEntry("code-910", "title-247")
        item.status = FallbackHoldEntryStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackHoldEntryStatus.CREATED) }
    }
}
