package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newHoldTemplate("code-36", "name-303")
        assertEquals(HoldTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newHoldTemplate("code-36", "name-303")
        try {
            item.moveTo(HoldTemplateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldTemplateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHoldTemplate("code-36", "name-303")
        item.status = HoldTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(HoldTemplateStatus.IDLE) }
    }
}
