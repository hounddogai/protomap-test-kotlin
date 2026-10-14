package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newTireTemplate("code-212", "name-340")
        assertEquals(TireTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTireTemplate("code-212", "name-340")
        try {
            item.moveTo(TireTemplateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireTemplateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTireTemplate("code-212", "name-340")
        item.status = TireTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(TireTemplateStatus.REQUESTED) }
    }
}
