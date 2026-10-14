package com.acme.transport.linehaul.relay.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRelayTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveRelayTemplate("code-239", "name-518")
        assertEquals(AdaptiveRelayTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newAdaptiveRelayTemplate("code-239", "name-518")
        try {
            item.moveTo(AdaptiveRelayTemplateStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRelayTemplateStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveRelayTemplate("code-239", "name-518")
        item.status = AdaptiveRelayTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveRelayTemplateStatus.PROPOSED) }
    }
}
