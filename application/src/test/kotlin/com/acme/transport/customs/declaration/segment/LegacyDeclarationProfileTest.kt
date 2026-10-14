package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDeclarationProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyDeclarationProfile("code-56", "label-455")
        assertEquals(LegacyDeclarationProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyDeclarationProfile("code-56", "label-455")
        try {
            item.moveTo(LegacyDeclarationProfileStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDeclarationProfileStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyDeclarationProfile("code-56", "label-455")
        item.status = LegacyDeclarationProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyDeclarationProfileStatus.PLANNED) }
    }
}
