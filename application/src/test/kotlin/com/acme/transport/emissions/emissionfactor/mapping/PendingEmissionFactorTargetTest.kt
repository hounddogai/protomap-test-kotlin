package com.acme.transport.emissions.emissionfactor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingEmissionFactorTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingEmissionFactorTarget("code-826", "name-326")
        assertEquals(PendingEmissionFactorTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newPendingEmissionFactorTarget("code-826", "name-326")
        try {
            item.moveTo(PendingEmissionFactorTargetStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingEmissionFactorTargetStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingEmissionFactorTarget("code-826", "name-326")
        item.status = PendingEmissionFactorTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingEmissionFactorTargetStatus.PLANNED) }
    }
}
