package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRelayVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalRelayVariance("code-653", "label-136")
        assertEquals(GlobalRelayVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalRelayVariance("code-653", "label-136")
        try {
            item.moveTo(GlobalRelayVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRelayVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalRelayVariance("code-653", "label-136")
        item.status = GlobalRelayVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalRelayVarianceStatus.PROPOSED) }
    }
}
