package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDeclarationTargetTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalDeclarationTarget("code-719", "name-11")
        assertEquals(RegionalDeclarationTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalDeclarationTarget("code-719", "name-11")
        try {
            item.moveTo(RegionalDeclarationTargetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDeclarationTargetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalDeclarationTarget("code-719", "name-11")
        item.status = RegionalDeclarationTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalDeclarationTargetStatus.OPEN) }
    }
}
