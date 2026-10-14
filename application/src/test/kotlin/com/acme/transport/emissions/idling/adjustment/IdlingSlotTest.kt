package com.acme.transport.emissions.idling.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingSlotTest {
    @Test
    fun `starts in open`() {
        val item = newIdlingSlot("code-805", "title-660")
        assertEquals(IdlingSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newIdlingSlot("code-805", "title-660")
        try {
            item.moveTo(IdlingSlotStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingSlotStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newIdlingSlot("code-805", "title-660")
        item.status = IdlingSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(IdlingSlotStatus.OPEN) }
    }
}
