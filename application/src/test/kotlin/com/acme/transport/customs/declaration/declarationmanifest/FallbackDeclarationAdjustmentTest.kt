package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDeclarationAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackDeclarationAdjustment("code-662", "name-600")
        assertEquals(FallbackDeclarationAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackDeclarationAdjustment("code-662", "name-600")
        try {
            item.moveTo(FallbackDeclarationAdjustmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDeclarationAdjustmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackDeclarationAdjustment("code-662", "name-600")
        item.status = FallbackDeclarationAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackDeclarationAdjustmentStatus.PROPOSED) }
    }
}
