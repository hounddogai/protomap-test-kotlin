package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientEvChargeThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newLenientEvChargeThreshold("code-333", "title-475")
        assertEquals(LenientEvChargeThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientEvChargeThreshold("code-333", "title-475")
        try {
            item.moveTo(LenientEvChargeThresholdStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientEvChargeThresholdStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientEvChargeThreshold("code-333", "title-475")
        item.status = LenientEvChargeThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientEvChargeThresholdStatus.OPEN) }
    }
}
