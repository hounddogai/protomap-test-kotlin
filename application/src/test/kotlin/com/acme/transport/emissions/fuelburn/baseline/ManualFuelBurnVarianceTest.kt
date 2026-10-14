package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFuelBurnVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualFuelBurnVariance("code-35", "title-50")
        assertEquals(ManualFuelBurnVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualFuelBurnVariance("code-35", "title-50")
        try {
            item.moveTo(ManualFuelBurnVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFuelBurnVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualFuelBurnVariance("code-35", "title-50")
        item.status = ManualFuelBurnVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualFuelBurnVarianceStatus.PROPOSED) }
    }
}
