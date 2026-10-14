package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionWindowTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryInspectionWindow("code-345", "title-518")
        assertEquals(PrimaryInspectionWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryInspectionWindow("code-345", "title-518")
        try {
            item.moveTo(PrimaryInspectionWindowStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionWindowStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryInspectionWindow("code-345", "title-518")
        item.status = PrimaryInspectionWindowStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryInspectionWindowStatus.NEW) }
    }
}
