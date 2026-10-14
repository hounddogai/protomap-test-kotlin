package com.acme.transport.network.hub.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualHubBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newManualHubBaseline("code-767", "title-517")
        assertEquals(ManualHubBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualHubBaseline("code-767", "title-517")
        try {
            item.moveTo(ManualHubBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualHubBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualHubBaseline("code-767", "title-517")
        item.status = ManualHubBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualHubBaselineStatus.REQUESTED) }
    }
}
