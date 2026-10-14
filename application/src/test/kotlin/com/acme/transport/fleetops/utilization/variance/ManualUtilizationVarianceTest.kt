package com.acme.transport.fleetops.utilization.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualUtilizationVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualUtilizationVariance("code-554", "label-245")
        assertEquals(ManualUtilizationVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualUtilizationVariance("code-554", "label-245")
        try {
            item.moveTo(ManualUtilizationVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualUtilizationVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualUtilizationVariance("code-554", "label-245")
        item.status = ManualUtilizationVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualUtilizationVarianceStatus.PROPOSED) }
    }
}
