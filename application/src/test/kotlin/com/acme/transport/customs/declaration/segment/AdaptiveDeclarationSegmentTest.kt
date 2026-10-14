package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDeclarationSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveDeclarationSegment("code-846", "name-151")
        assertEquals(AdaptiveDeclarationSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveDeclarationSegment("code-846", "name-151")
        try {
            item.moveTo(AdaptiveDeclarationSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDeclarationSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveDeclarationSegment("code-846", "name-151")
        item.status = AdaptiveDeclarationSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveDeclarationSegmentStatus.OPEN) }
    }
}
