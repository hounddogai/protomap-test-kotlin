package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyEvChargeVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyEvChargeVariance("code-4", "title-661")
        assertEquals(LegacyEvChargeVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyEvChargeVariance("code-4", "title-661")
        try {
            item.moveTo(LegacyEvChargeVarianceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyEvChargeVarianceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyEvChargeVariance("code-4", "title-661")
        item.status = LegacyEvChargeVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyEvChargeVarianceStatus.CREATED) }
    }
}
