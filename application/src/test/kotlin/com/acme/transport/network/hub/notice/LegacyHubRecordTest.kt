package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyHubRecordTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyHubRecord("code-452", "title-373")
        assertEquals(LegacyHubRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyHubRecord("code-452", "title-373")
        try {
            item.moveTo(LegacyHubRecordStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyHubRecordStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyHubRecord("code-452", "title-373")
        item.status = LegacyHubRecordStatus.FAILED
        assertFails("move") { item.moveTo(LegacyHubRecordStatus.NEW) }
    }
}
