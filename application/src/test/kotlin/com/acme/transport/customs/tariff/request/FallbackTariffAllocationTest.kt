package com.acme.transport.customs.tariff.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTariffAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackTariffAllocation("code-66", "label-400")
        assertEquals(FallbackTariffAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newFallbackTariffAllocation("code-66", "label-400")
        try {
            item.moveTo(FallbackTariffAllocationStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTariffAllocationStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackTariffAllocation("code-66", "label-400")
        item.status = FallbackTariffAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackTariffAllocationStatus.PROPOSED) }
    }
}
