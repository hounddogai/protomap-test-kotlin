package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackHubMappingTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackHubMapping("code-530", "title-436")
        assertEquals(FallbackHubMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackHubMapping("code-530", "title-436")
        try {
            item.moveTo(FallbackHubMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackHubMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackHubMapping("code-530", "title-436")
        item.status = FallbackHubMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackHubMappingStatus.OPEN) }
    }
}
