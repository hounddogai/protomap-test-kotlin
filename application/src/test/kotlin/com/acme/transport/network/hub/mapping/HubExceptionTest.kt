package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newHubException("code-426", "label-997")
        assertEquals(HubExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newHubException("code-426", "label-997")
        try {
            item.moveTo(HubExceptionStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubExceptionStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newHubException("code-426", "label-997")
        item.status = HubExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(HubExceptionStatus.OPEN) }
    }
}
