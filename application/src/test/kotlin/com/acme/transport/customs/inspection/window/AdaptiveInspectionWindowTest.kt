package com.acme.transport.customs.inspection.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveInspectionWindow("code-853", "label-558")
        assertEquals(AdaptiveInspectionWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveInspectionWindow("code-853", "label-558")
        try {
            item.moveTo(AdaptiveInspectionWindowStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionWindowStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveInspectionWindow("code-853", "label-558")
        item.status = AdaptiveInspectionWindowStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveInspectionWindowStatus.DRAFT) }
    }
}
