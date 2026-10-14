package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerWindowTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerWindow("code-382", "label-895")
        assertEquals(BrokerWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBrokerWindow("code-382", "label-895")
        try {
            item.moveTo(BrokerWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerWindow("code-382", "label-895")
        item.status = BrokerWindowStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerWindowStatus.CREATED) }
    }
}
