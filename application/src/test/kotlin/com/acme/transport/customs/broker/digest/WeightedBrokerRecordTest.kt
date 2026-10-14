package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBrokerRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedBrokerRecord("code-186", "name-621")
        assertEquals(WeightedBrokerRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedBrokerRecord("code-186", "name-621")
        try {
            item.moveTo(WeightedBrokerRecordStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBrokerRecordStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedBrokerRecord("code-186", "name-621")
        item.status = WeightedBrokerRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedBrokerRecordStatus.PLANNED) }
    }
}
