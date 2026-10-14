package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newDwellTemplate("code-536", "name-692")
        assertEquals(DwellTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDwellTemplate("code-536", "name-692")
        try {
            item.moveTo(DwellTemplateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellTemplateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDwellTemplate("code-536", "name-692")
        item.status = DwellTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(DwellTemplateStatus.PLANNED) }
    }
}
