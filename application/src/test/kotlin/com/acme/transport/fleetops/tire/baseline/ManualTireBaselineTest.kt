package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTireBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newManualTireBaseline("code-533", "title-608")
        assertEquals(ManualTireBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualTireBaseline("code-533", "title-608")
        try {
            item.moveTo(ManualTireBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTireBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualTireBaseline("code-533", "title-608")
        item.status = ManualTireBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualTireBaselineStatus.REQUESTED) }
    }
}
