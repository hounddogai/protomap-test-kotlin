package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDeclarationThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyDeclarationThreshold("code-603", "title-55")
        assertEquals(LegacyDeclarationThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLegacyDeclarationThreshold("code-603", "title-55")
        try {
            item.moveTo(LegacyDeclarationThresholdStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDeclarationThresholdStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyDeclarationThreshold("code-603", "title-55")
        item.status = LegacyDeclarationThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyDeclarationThresholdStatus.DRAFT) }
    }
}
