package com.acme.transport.customs.declaration.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDeclarationReviewTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultDeclarationReview("code-941", "name-180")
        assertEquals(DefaultDeclarationReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultDeclarationReview("code-941", "name-180")
        try {
            item.moveTo(DefaultDeclarationReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDeclarationReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultDeclarationReview("code-941", "name-180")
        item.status = DefaultDeclarationReviewStatus.FAILED
        assertFails("move") { item.moveTo(DefaultDeclarationReviewStatus.NEW) }
    }
}
