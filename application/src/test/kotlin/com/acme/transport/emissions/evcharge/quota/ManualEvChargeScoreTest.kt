package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualEvChargeScoreTest {
    @Test
    fun `starts in new`() {
        val item = newManualEvChargeScore("code-243", "name-537")
        assertEquals(ManualEvChargeScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newManualEvChargeScore("code-243", "name-537")
        try {
            item.moveTo(ManualEvChargeScoreStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualEvChargeScoreStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualEvChargeScore("code-243", "name-537")
        item.status = ManualEvChargeScoreStatus.FAILED
        assertFails("move") { item.moveTo(ManualEvChargeScoreStatus.NEW) }
    }
}
