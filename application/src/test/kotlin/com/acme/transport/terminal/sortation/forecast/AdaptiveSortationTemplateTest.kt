package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveSortationTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveSortationTemplate("code-530", "title-272")
        assertEquals(AdaptiveSortationTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAdaptiveSortationTemplate("code-530", "title-272")
        try {
            item.moveTo(AdaptiveSortationTemplateStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveSortationTemplateStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveSortationTemplate("code-530", "title-272")
        item.status = AdaptiveSortationTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveSortationTemplateStatus.PLANNED) }
    }
}
