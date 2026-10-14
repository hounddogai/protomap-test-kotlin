package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEvChargeThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackEvChargeThreshold("code-574", "title-52")
        assertEquals(FallbackEvChargeThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFallbackEvChargeThreshold("code-574", "title-52")
        try {
            item.moveTo(FallbackEvChargeThresholdStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEvChargeThresholdStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackEvChargeThreshold("code-574", "title-52")
        item.status = FallbackEvChargeThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackEvChargeThresholdStatus.DRAFT) }
    }
}
