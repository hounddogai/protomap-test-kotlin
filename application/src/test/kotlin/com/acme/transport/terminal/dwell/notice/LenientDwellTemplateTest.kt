package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDwellTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientDwellTemplate("code-946", "title-609")
        assertEquals(LenientDwellTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientDwellTemplate("code-946", "title-609")
        try {
            item.moveTo(LenientDwellTemplateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDwellTemplateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientDwellTemplate("code-946", "title-609")
        item.status = LenientDwellTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientDwellTemplateStatus.REQUESTED) }
    }
}
