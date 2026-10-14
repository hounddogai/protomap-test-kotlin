package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDeclarationBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newStrictDeclarationBaseline("code-667", "name-708")
        assertEquals(StrictDeclarationBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictDeclarationBaseline("code-667", "name-708")
        try {
            item.moveTo(StrictDeclarationBaselineStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDeclarationBaselineStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictDeclarationBaseline("code-667", "name-708")
        item.status = StrictDeclarationBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictDeclarationBaselineStatus.OPEN) }
    }
}
