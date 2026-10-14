package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEmissionFactorPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newStrictEmissionFactorPolicy("code-241", "name-222")
        assertEquals(StrictEmissionFactorPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictEmissionFactorPolicy("code-241", "name-222")
        try {
            item.moveTo(StrictEmissionFactorPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEmissionFactorPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictEmissionFactorPolicy("code-241", "name-222")
        item.status = StrictEmissionFactorPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(StrictEmissionFactorPolicyStatus.CREATED) }
    }
}
