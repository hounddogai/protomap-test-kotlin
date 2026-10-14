package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedEmissionFactorDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedEmissionFactorDigest("code-681", "title-472")
        assertEquals(BatchedEmissionFactorDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedEmissionFactorDigest("code-681", "title-472")
        try {
            item.moveTo(BatchedEmissionFactorDigestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedEmissionFactorDigestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedEmissionFactorDigest("code-681", "title-472")
        item.status = BatchedEmissionFactorDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedEmissionFactorDigestStatus.PROPOSED) }
    }
}
