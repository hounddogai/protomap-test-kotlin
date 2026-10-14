package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingProofOfDeliveryCycleTest {
    @Test
    fun `starts in new`() {
        val item = newRollingProofOfDeliveryCycle("code-862", "title-700")
        assertEquals(RollingProofOfDeliveryCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingProofOfDeliveryCycle("code-862", "title-700")
        try {
            item.moveTo(RollingProofOfDeliveryCycleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingProofOfDeliveryCycleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingProofOfDeliveryCycle("code-862", "title-700")
        item.status = RollingProofOfDeliveryCycleStatus.FAILED
        assertFails("move") { item.moveTo(RollingProofOfDeliveryCycleStatus.NEW) }
    }
}
