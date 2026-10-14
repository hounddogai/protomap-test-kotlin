package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEvChargeRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackEvChargeRecord("code-824", "title-2")
        assertEquals(FallbackEvChargeRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackEvChargeRecord("code-824", "title-2")
        try {
            item.moveTo(FallbackEvChargeRecordStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEvChargeRecordStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackEvChargeRecord("code-824", "title-2")
        item.status = FallbackEvChargeRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackEvChargeRecordStatus.PROPOSED) }
    }
}
