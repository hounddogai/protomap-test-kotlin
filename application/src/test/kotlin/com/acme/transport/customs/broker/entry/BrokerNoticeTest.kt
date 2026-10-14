package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerNotice("code-267", "label-68")
        assertEquals(BrokerNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBrokerNotice("code-267", "label-68")
        try {
            item.moveTo(BrokerNoticeStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerNoticeStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerNotice("code-267", "label-68")
        item.status = BrokerNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerNoticeStatus.CREATED) }
    }
}
