package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newTariffVariance("code-234", "label-571")
        assertEquals(TariffVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTariffVariance("code-234", "label-571")
        try {
            item.moveTo(TariffVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTariffVariance("code-234", "label-571")
        item.status = TariffVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TariffVarianceStatus.PROPOSED) }
    }
}
