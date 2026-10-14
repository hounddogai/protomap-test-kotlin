package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHubMappingTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultHubMapping("code-732", "title-840")
        assertEquals(DefaultHubMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultHubMapping("code-732", "title-840")
        try {
            item.moveTo(DefaultHubMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHubMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultHubMapping("code-732", "title-840")
        item.status = DefaultHubMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultHubMappingStatus.OPEN) }
    }
}
