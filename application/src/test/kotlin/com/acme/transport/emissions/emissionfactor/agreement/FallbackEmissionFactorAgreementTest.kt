package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEmissionFactorAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackEmissionFactorAgreement("code-130", "title-329")
        assertEquals(FallbackEmissionFactorAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackEmissionFactorAgreement("code-130", "title-329")
        try {
            item.moveTo(FallbackEmissionFactorAgreementStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEmissionFactorAgreementStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackEmissionFactorAgreement("code-130", "title-329")
        item.status = FallbackEmissionFactorAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackEmissionFactorAgreementStatus.PROPOSED) }
    }
}
