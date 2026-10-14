package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newEmissionFactorPolicy("code-930", "label-361")
        assertEquals(EmissionFactorPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newEmissionFactorPolicy("code-930", "label-361")
        try {
            item.moveTo(EmissionFactorPolicyStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorPolicyStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newEmissionFactorPolicy("code-930", "label-361")
        item.status = EmissionFactorPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(EmissionFactorPolicyStatus.PROPOSED) }
    }
}
