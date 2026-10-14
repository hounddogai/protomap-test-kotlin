package com.acme.transport.customs.declaration.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDeclarationReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackDeclarationReview("code-37", "name-88")
        assertEquals(FallbackDeclarationReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackDeclarationReview("code-37", "name-88")
        try {
            item.moveTo(FallbackDeclarationReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDeclarationReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackDeclarationReview("code-37", "name-88")
        item.status = FallbackDeclarationReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackDeclarationReviewStatus.PROPOSED) }
    }
}
