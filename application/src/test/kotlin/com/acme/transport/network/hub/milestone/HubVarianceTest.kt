package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newHubVariance("code-671", "name-666")
        assertEquals(HubVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newHubVariance("code-671", "name-666")
        try {
            item.moveTo(HubVarianceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubVarianceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHubVariance("code-671", "name-666")
        item.status = HubVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(HubVarianceStatus.CREATED) }
    }
}
