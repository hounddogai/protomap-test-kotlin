package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingHoldEntryTest {
    @Test
    fun `starts in created`() {
        val item = newPendingHoldEntry("code-343", "title-210")
        assertEquals(PendingHoldEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingHoldEntry("code-343", "title-210")
        try {
            item.moveTo(PendingHoldEntryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingHoldEntryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingHoldEntry("code-343", "title-210")
        item.status = PendingHoldEntryStatus.RETIRED
        assertFails("move") { item.moveTo(PendingHoldEntryStatus.CREATED) }
    }
}
