package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualEvChargeTierTest {
    @Test
    fun `starts in idle`() {
        val item = newManualEvChargeTier("code-831", "title-41")
        assertEquals(ManualEvChargeTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newManualEvChargeTier("code-831", "title-41")
        try {
            item.moveTo(ManualEvChargeTierStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualEvChargeTierStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualEvChargeTier("code-831", "title-41")
        item.status = ManualEvChargeTierStatus.STOPPED
        assertFails("move") { item.moveTo(ManualEvChargeTierStatus.IDLE) }
    }
}
