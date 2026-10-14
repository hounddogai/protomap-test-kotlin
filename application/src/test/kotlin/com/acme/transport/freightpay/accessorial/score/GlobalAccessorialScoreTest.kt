package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAccessorialScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalAccessorialScore("code-498", "label-857")
        assertEquals(GlobalAccessorialScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalAccessorialScore("code-498", "label-857")
        try {
            item.moveTo(GlobalAccessorialScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAccessorialScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalAccessorialScore("code-498", "label-857")
        item.status = GlobalAccessorialScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalAccessorialScoreStatus.PROPOSED) }
    }
}
