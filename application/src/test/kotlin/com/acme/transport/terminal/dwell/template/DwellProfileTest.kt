package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newDwellProfile("code-275", "title-39")
        assertEquals(DwellProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDwellProfile("code-275", "title-39")
        try {
            item.moveTo(DwellProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDwellProfile("code-275", "title-39")
        item.status = DwellProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(DwellProfileStatus.REQUESTED) }
    }
}
