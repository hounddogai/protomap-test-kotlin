package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEmissionFactorBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredEmissionFactorBatch("code-921", "label-907")
        assertEquals(TieredEmissionFactorBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTieredEmissionFactorBatch("code-921", "label-907")
        try {
            item.moveTo(TieredEmissionFactorBatchStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEmissionFactorBatchStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredEmissionFactorBatch("code-921", "label-907")
        item.status = TieredEmissionFactorBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredEmissionFactorBatchStatus.PROPOSED) }
    }
}
