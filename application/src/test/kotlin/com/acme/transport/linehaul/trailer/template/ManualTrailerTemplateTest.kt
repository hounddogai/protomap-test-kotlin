package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newManualTrailerTemplate("code-673", "title-331")
        assertEquals(ManualTrailerTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualTrailerTemplate("code-673", "title-331")
        try {
            item.moveTo(ManualTrailerTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualTrailerTemplate("code-673", "title-331")
        item.status = ManualTrailerTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(ManualTrailerTemplateStatus.DRAFT) }
    }
}
