package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingEntryTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingEntry("code-192", "name-177")
        assertEquals(IdlingEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIdlingEntry("code-192", "name-177")
        try {
            item.moveTo(IdlingEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingEntry("code-192", "name-177")
        item.status = IdlingEntryStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingEntryStatus.CREATED) }
    }
}
