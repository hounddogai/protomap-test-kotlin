package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredHubExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newTieredHubException("code-123", "name-971")
        assertEquals(TieredHubExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTieredHubException("code-123", "name-971")
        try {
            item.moveTo(TieredHubExceptionStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredHubExceptionStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredHubException("code-123", "name-971")
        item.status = TieredHubExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(TieredHubExceptionStatus.CREATED) }
    }
}
