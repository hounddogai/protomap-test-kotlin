package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionWindowTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionWindow("code-516", "name-968")
        assertEquals(InspectionWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newInspectionWindow("code-516", "name-968")
        try {
            item.moveTo(InspectionWindowStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionWindowStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionWindow("code-516", "name-968")
        item.status = InspectionWindowStatus.FAILED
        assertFails("move") { item.moveTo(InspectionWindowStatus.NEW) }
    }
}
