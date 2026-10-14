package com.acme.transport.lastmile.proofofdelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryForecastTest {
    @Test
    fun `starts in new`() {
        val item = newProofOfDeliveryForecast("code-704", "title-793")
        assertEquals(ProofOfDeliveryForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newProofOfDeliveryForecast("code-704", "title-793")
        try {
            item.moveTo(ProofOfDeliveryForecastStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryForecastStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newProofOfDeliveryForecast("code-704", "title-793")
        item.status = ProofOfDeliveryForecastStatus.FAILED
        assertFails("move") { item.moveTo(ProofOfDeliveryForecastStatus.NEW) }
    }
}
