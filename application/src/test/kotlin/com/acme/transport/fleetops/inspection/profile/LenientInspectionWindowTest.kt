package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientInspectionWindowTest {
    @Test
    fun `starts in new`() {
        val item = newLenientInspectionWindow("code-69", "label-25")
        assertEquals(LenientInspectionWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newLenientInspectionWindow("code-69", "label-25")
        try {
            item.moveTo(LenientInspectionWindowStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientInspectionWindowStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientInspectionWindow("code-69", "label-25")
        item.status = LenientInspectionWindowStatus.FAILED
        assertFails("move") { item.moveTo(LenientInspectionWindowStatus.NEW) }
    }
}
