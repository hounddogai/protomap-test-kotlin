package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictServiceAreaAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newStrictServiceAreaAssignment("code-748", "name-382")
        assertEquals(StrictServiceAreaAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newStrictServiceAreaAssignment("code-748", "name-382")
        try {
            item.moveTo(StrictServiceAreaAssignmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictServiceAreaAssignmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictServiceAreaAssignment("code-748", "name-382")
        item.status = StrictServiceAreaAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(StrictServiceAreaAssignmentStatus.NEW) }
    }
}
