package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newDeclarationReview("code-998", "title-623")
        assertEquals(DeclarationReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDeclarationReview("code-998", "title-623")
        try {
            item.moveTo(DeclarationReviewStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationReviewStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDeclarationReview("code-998", "title-623")
        item.status = DeclarationReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(DeclarationReviewStatus.REQUESTED) }
    }
}
