package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newDeclarationEstimate("code-855", "title-335")
        assertEquals(DeclarationEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDeclarationEstimate("code-855", "title-335")
        try {
            item.moveTo(DeclarationEstimateStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationEstimateStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDeclarationEstimate("code-855", "title-335")
        item.status = DeclarationEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(DeclarationEstimateStatus.DRAFT) }
    }
}
