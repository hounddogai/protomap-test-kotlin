package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionWindowTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionWindow("code-533", "label-140")
        assertEquals(InspectionWindowStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newInspectionWindow("code-533", "label-140")
        try {
            item.moveTo(InspectionWindowStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionWindowStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionWindow("code-533", "label-140")
        item.status = InspectionWindowStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionWindowStatus.PLANNED) }
    }
}
