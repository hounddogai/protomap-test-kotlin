package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryEvChargeThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryEvChargeThreshold("code-472", "name-292")
        assertEquals(PrimaryEvChargeThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryEvChargeThreshold("code-472", "name-292")
        try {
            item.moveTo(PrimaryEvChargeThresholdStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryEvChargeThresholdStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryEvChargeThreshold("code-472", "name-292")
        item.status = PrimaryEvChargeThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryEvChargeThresholdStatus.OPEN) }
    }
}
