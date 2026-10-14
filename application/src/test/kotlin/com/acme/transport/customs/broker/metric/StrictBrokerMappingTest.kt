package com.acme.transport.customs.broker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictBrokerMappingTest {
    @Test
    fun `starts in created`() {
        val item = newStrictBrokerMapping("code-870", "label-820")
        assertEquals(StrictBrokerMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newStrictBrokerMapping("code-870", "label-820")
        try {
            item.moveTo(StrictBrokerMappingStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictBrokerMappingStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictBrokerMapping("code-870", "label-820")
        item.status = StrictBrokerMappingStatus.RETIRED
        assertFails("move") { item.moveTo(StrictBrokerMappingStatus.CREATED) }
    }
}
