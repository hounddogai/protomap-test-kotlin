package com.acme.transport.emissions.evcharge.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeManifestTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeManifest("code-77", "title-496")
        assertEquals(EvChargeManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEvChargeManifest("code-77", "title-496")
        try {
            item.moveTo(EvChargeManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeManifest("code-77", "title-496")
        item.status = EvChargeManifestStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeManifestStatus.NEW) }
    }
}
