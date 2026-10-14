package com.acme.transport.linehaul.relay.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRelayTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultRelayTemplate("code-421", "label-662")
        assertEquals(DefaultRelayTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDefaultRelayTemplate("code-421", "label-662")
        try {
            item.moveTo(DefaultRelayTemplateStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRelayTemplateStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultRelayTemplate("code-421", "label-662")
        item.status = DefaultRelayTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultRelayTemplateStatus.CREATED) }
    }
}
