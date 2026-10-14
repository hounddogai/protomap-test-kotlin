package com.acme.transport.emissions.idling.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newIdlingMapping("code-219", "title-128")
        assertEquals(IdlingMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newIdlingMapping("code-219", "title-128")
        try {
            item.moveTo(IdlingMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newIdlingMapping("code-219", "title-128")
        item.status = IdlingMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(IdlingMappingStatus.REQUESTED) }
    }
}
