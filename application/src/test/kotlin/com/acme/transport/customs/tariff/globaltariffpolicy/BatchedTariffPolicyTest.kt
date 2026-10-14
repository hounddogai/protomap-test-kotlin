package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTariffPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedTariffPolicy("code-171", "name-893")
        assertEquals(BatchedTariffPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedTariffPolicy("code-171", "name-893")
        try {
            item.moveTo(BatchedTariffPolicyStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTariffPolicyStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedTariffPolicy("code-171", "name-893")
        item.status = BatchedTariffPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedTariffPolicyStatus.IDLE) }
    }
}
