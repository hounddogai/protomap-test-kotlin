package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newDefectException("code-851", "name-798")
        assertEquals(DefectExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefectException("code-851", "name-798")
        try {
            item.moveTo(DefectExceptionStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectExceptionStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefectException("code-851", "name-798")
        item.status = DefectExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(DefectExceptionStatus.PLANNED) }
    }
}
