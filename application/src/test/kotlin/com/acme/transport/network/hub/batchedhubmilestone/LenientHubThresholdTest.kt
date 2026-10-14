package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientHubThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientHubThreshold("code-981", "name-185")
        assertEquals(LenientHubThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLenientHubThreshold("code-981", "name-185")
        try {
            item.moveTo(LenientHubThresholdStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientHubThresholdStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientHubThreshold("code-981", "name-185")
        item.status = LenientHubThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientHubThresholdStatus.PROPOSED) }
    }
}
