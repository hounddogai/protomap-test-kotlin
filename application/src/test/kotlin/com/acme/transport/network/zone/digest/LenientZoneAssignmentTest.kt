package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientZoneAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newLenientZoneAssignment("code-749", "title-203")
        assertEquals(LenientZoneAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientZoneAssignment("code-749", "title-203")
        try {
            item.moveTo(LenientZoneAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientZoneAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientZoneAssignment("code-749", "title-203")
        item.status = LenientZoneAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(LenientZoneAssignmentStatus.CREATED) }
    }
}
