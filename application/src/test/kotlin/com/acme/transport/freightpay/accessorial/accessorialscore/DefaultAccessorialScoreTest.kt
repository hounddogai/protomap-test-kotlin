package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAccessorialScoreTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultAccessorialScore("code-243", "label-136")
        assertEquals(DefaultAccessorialScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultAccessorialScore("code-243", "label-136")
        try {
            item.moveTo(DefaultAccessorialScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAccessorialScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultAccessorialScore("code-243", "label-136")
        item.status = DefaultAccessorialScoreStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultAccessorialScoreStatus.CREATED) }
    }
}
