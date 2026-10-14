package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDeclarationTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackDeclarationTarget("code-779", "title-619")
        assertEquals(FallbackDeclarationTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackDeclarationTarget("code-779", "title-619")
        try {
            item.moveTo(FallbackDeclarationTargetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDeclarationTargetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackDeclarationTarget("code-779", "title-619")
        item.status = FallbackDeclarationTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackDeclarationTargetStatus.PROPOSED) }
    }
}
