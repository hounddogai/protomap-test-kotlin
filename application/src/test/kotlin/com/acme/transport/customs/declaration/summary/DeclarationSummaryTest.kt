package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newDeclarationSummary("code-772", "name-889")
        assertEquals(DeclarationSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDeclarationSummary("code-772", "name-889")
        try {
            item.moveTo(DeclarationSummaryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationSummaryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDeclarationSummary("code-772", "name-889")
        item.status = DeclarationSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(DeclarationSummaryStatus.OPEN) }
    }
}
