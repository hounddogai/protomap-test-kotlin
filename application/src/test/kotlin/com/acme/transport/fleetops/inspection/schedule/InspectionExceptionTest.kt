package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionException("code-801", "name-938")
        assertEquals(InspectionExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newInspectionException("code-801", "name-938")
        try {
            item.moveTo(InspectionExceptionStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionExceptionStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionException("code-801", "name-938")
        item.status = InspectionExceptionStatus.FAILED
        assertFails("move") { item.moveTo(InspectionExceptionStatus.NEW) }
    }
}
