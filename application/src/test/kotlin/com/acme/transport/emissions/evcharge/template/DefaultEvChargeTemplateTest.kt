package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEvChargeTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultEvChargeTemplate("code-114", "title-171")
        assertEquals(DefaultEvChargeTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefaultEvChargeTemplate("code-114", "title-171")
        try {
            item.moveTo(DefaultEvChargeTemplateStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEvChargeTemplateStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultEvChargeTemplate("code-114", "title-171")
        item.status = DefaultEvChargeTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultEvChargeTemplateStatus.IDLE) }
    }
}
