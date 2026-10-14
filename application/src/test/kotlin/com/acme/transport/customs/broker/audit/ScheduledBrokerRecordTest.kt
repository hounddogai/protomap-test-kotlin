package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledBrokerRecordTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledBrokerRecord("code-135", "label-68")
        assertEquals(ScheduledBrokerRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScheduledBrokerRecord("code-135", "label-68")
        try {
            item.moveTo(ScheduledBrokerRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledBrokerRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledBrokerRecord("code-135", "label-68")
        item.status = ScheduledBrokerRecordStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledBrokerRecordStatus.CREATED) }
    }
}
