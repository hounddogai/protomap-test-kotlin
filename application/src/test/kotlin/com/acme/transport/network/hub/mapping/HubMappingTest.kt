package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubMappingTest {
    @Test
    fun `starts in created`() {
        val item = newHubMapping("code-448", "name-98")
        assertEquals(HubMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newHubMapping("code-448", "name-98")
        try {
            item.moveTo(HubMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHubMapping("code-448", "name-98")
        item.status = HubMappingStatus.RETIRED
        assertFails("move") { item.moveTo(HubMappingStatus.CREATED) }
    }
}
