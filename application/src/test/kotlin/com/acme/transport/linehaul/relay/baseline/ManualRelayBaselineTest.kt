package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRelayBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newManualRelayBaseline("code-385", "name-614")
        assertEquals(ManualRelayBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newManualRelayBaseline("code-385", "name-614")
        try {
            item.moveTo(ManualRelayBaselineStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRelayBaselineStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualRelayBaseline("code-385", "name-614")
        item.status = ManualRelayBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualRelayBaselineStatus.OPEN) }
    }
}
