package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingIdlingEntryTest {
    @Test
    fun `starts in created`() {
        val item = newPendingIdlingEntry("code-212", "label-70")
        assertEquals(PendingIdlingEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingIdlingEntry("code-212", "label-70")
        try {
            item.moveTo(PendingIdlingEntryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingIdlingEntryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingIdlingEntry("code-212", "label-70")
        item.status = PendingIdlingEntryStatus.RETIRED
        assertFails("move") { item.moveTo(PendingIdlingEntryStatus.CREATED) }
    }
}
