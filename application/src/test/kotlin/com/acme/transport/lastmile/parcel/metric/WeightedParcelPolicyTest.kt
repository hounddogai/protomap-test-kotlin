package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedParcelPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedParcelPolicy("code-980", "label-963")
        assertEquals(WeightedParcelPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedParcelPolicy("code-980", "label-963")
        try {
            item.moveTo(WeightedParcelPolicyStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedParcelPolicyStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedParcelPolicy("code-980", "label-963")
        item.status = WeightedParcelPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedParcelPolicyStatus.PROPOSED) }
    }
}
