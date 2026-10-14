package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultIdlingEntryTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultIdlingEntry("code-825", "name-700")
        assertEquals(DefaultIdlingEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultIdlingEntry("code-825", "name-700")
        try {
            item.moveTo(DefaultIdlingEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultIdlingEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultIdlingEntry("code-825", "name-700")
        item.status = DefaultIdlingEntryStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultIdlingEntryStatus.CREATED) }
    }
}
