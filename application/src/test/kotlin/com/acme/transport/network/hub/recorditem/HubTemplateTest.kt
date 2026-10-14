package com.acme.transport.network.hub.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newHubTemplate("code-287", "title-351")
        assertEquals(HubTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newHubTemplate("code-287", "title-351")
        try {
            item.moveTo(HubTemplateStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubTemplateStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHubTemplate("code-287", "title-351")
        item.status = HubTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(HubTemplateStatus.REQUESTED) }
    }
}
