package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalParcelAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalParcelAssignment("code-691", "name-304")
        assertEquals(GlobalParcelAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalParcelAssignment("code-691", "name-304")
        try {
            item.moveTo(GlobalParcelAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalParcelAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalParcelAssignment("code-691", "name-304")
        item.status = GlobalParcelAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalParcelAssignmentStatus.REQUESTED) }
    }
}
