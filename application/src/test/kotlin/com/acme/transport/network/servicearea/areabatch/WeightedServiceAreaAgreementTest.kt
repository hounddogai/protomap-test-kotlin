package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedServiceAreaAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedServiceAreaAgreement("code-450", "name-321")
        assertEquals(WeightedServiceAreaAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedServiceAreaAgreement("code-450", "name-321")
        try {
            item.moveTo(WeightedServiceAreaAgreementStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedServiceAreaAgreementStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedServiceAreaAgreement("code-450", "name-321")
        item.status = WeightedServiceAreaAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedServiceAreaAgreementStatus.PROPOSED) }
    }
}
