package com.acme.transport.freightpay.accessorial.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAccessorialTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackAccessorialTemplate("code-278", "label-35")
        assertEquals(FallbackAccessorialTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackAccessorialTemplate("code-278", "label-35")
        try {
            item.moveTo(FallbackAccessorialTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAccessorialTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackAccessorialTemplate("code-278", "label-35")
        item.status = FallbackAccessorialTemplateStatus.FAILED
        assertFails("move") { item.moveTo(FallbackAccessorialTemplateStatus.NEW) }
    }
}
