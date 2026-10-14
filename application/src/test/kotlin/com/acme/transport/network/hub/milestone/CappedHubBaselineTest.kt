package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedHubBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newCappedHubBaseline("code-324", "label-393")
        assertEquals(CappedHubBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newCappedHubBaseline("code-324", "label-393")
        try {
            item.moveTo(CappedHubBaselineStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedHubBaselineStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedHubBaseline("code-324", "label-393")
        item.status = CappedHubBaselineStatus.FAILED
        assertFails("move") { item.moveTo(CappedHubBaselineStatus.NEW) }
    }
}
