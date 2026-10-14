package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDwellVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedDwellVariance("code-713", "title-921")
        assertEquals(WeightedDwellVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedDwellVariance("code-713", "title-921")
        try {
            item.moveTo(WeightedDwellVarianceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDwellVarianceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedDwellVariance("code-713", "title-921")
        item.status = WeightedDwellVarianceStatus.FAILED
        assertFails("move") { item.moveTo(WeightedDwellVarianceStatus.NEW) }
    }
}
