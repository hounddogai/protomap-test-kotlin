package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedIdlingVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedIdlingVariance("code-319", "name-21")
        assertEquals(BatchedIdlingVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedIdlingVariance("code-319", "name-21")
        try {
            item.moveTo(BatchedIdlingVarianceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedIdlingVarianceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedIdlingVariance("code-319", "name-21")
        item.status = BatchedIdlingVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedIdlingVarianceStatus.CREATED) }
    }
}
