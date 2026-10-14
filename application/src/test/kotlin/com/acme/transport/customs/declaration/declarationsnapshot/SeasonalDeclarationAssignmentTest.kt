package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDeclarationAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalDeclarationAssignment("code-361", "name-416")
        assertEquals(SeasonalDeclarationAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newSeasonalDeclarationAssignment("code-361", "name-416")
        try {
            item.moveTo(SeasonalDeclarationAssignmentStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDeclarationAssignmentStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalDeclarationAssignment("code-361", "name-416")
        item.status = SeasonalDeclarationAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalDeclarationAssignmentStatus.DRAFT) }
    }
}
