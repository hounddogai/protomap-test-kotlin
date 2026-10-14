package com.acme.transport.network.hub.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalHubNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalHubNotice("code-798", "label-883")
        assertEquals(RegionalHubNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRegionalHubNotice("code-798", "label-883")
        try {
            item.moveTo(RegionalHubNoticeStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalHubNoticeStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalHubNotice("code-798", "label-883")
        item.status = RegionalHubNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalHubNoticeStatus.PROPOSED) }
    }
}
