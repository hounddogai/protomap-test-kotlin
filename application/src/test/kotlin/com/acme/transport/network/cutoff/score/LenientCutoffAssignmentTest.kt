package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCutoffAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newLenientCutoffAssignment("code-142", "name-748")
        assertEquals(LenientCutoffAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientCutoffAssignment("code-142", "name-748")
        try {
            item.moveTo(LenientCutoffAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCutoffAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientCutoffAssignment("code-142", "name-748")
        item.status = LenientCutoffAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(LenientCutoffAssignmentStatus.CREATED) }
    }
}
