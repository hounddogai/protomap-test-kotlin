package com.acme.transport.emissions.emissionfactor.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEmissionFactorBatchTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultEmissionFactorBatch("code-472", "label-756")
        assertEquals(DefaultEmissionFactorBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDefaultEmissionFactorBatch("code-472", "label-756")
        try {
            item.moveTo(DefaultEmissionFactorBatchStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEmissionFactorBatchStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultEmissionFactorBatch("code-472", "label-756")
        item.status = DefaultEmissionFactorBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultEmissionFactorBatchStatus.OPEN) }
    }
}
