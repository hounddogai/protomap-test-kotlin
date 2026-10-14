package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedHubMappingTest {
    @Test
    fun `starts in new`() {
        val item = newCappedHubMapping("code-49", "name-225")
        assertEquals(CappedHubMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCappedHubMapping("code-49", "name-225")
        try {
            item.moveTo(CappedHubMappingStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedHubMappingStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedHubMapping("code-49", "name-225")
        item.status = CappedHubMappingStatus.FAILED
        assertFails("move") { item.moveTo(CappedHubMappingStatus.NEW) }
    }
}
