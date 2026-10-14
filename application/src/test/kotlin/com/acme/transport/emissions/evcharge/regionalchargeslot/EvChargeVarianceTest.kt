package com.acme.transport.emissions.evcharge.regionalchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newEvChargeVariance("code-639", "name-984")
        assertEquals(EvChargeVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newEvChargeVariance("code-639", "name-984")
        try {
            item.moveTo(EvChargeVarianceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeVarianceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEvChargeVariance("code-639", "name-984")
        item.status = EvChargeVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(EvChargeVarianceStatus.CREATED) }
    }
}
