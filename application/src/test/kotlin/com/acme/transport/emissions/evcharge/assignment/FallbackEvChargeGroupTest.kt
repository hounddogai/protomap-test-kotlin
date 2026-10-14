package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEvChargeGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackEvChargeGroup("code-660", "title-67")
        assertEquals(FallbackEvChargeGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackEvChargeGroup("code-660", "title-67")
        try {
            item.moveTo(FallbackEvChargeGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEvChargeGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackEvChargeGroup("code-660", "title-67")
        item.status = FallbackEvChargeGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackEvChargeGroupStatus.REQUESTED) }
    }
}
