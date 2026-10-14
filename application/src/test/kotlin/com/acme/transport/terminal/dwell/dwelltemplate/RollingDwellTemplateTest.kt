package com.acme.transport.terminal.dwell.dwelltemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDwellTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingDwellTemplate("code-53", "name-52")
        assertEquals(RollingDwellTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRollingDwellTemplate("code-53", "name-52")
        try {
            item.moveTo(RollingDwellTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDwellTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingDwellTemplate("code-53", "name-52")
        item.status = RollingDwellTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(RollingDwellTemplateStatus.DRAFT) }
    }
}
