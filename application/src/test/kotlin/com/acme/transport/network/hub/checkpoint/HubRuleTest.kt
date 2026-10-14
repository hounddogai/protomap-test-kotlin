package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubRuleTest {
    @Test
    fun `starts in open`() {
        val item = newHubRule("code-635", "title-465")
        assertEquals(HubRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newHubRule("code-635", "title-465")
        try {
            item.moveTo(HubRuleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubRuleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newHubRule("code-635", "title-465")
        item.status = HubRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(HubRuleStatus.OPEN) }
    }
}
