package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialScore("code-136", "label-146")
        assertEquals(AccessorialScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAccessorialScore("code-136", "label-146")
        try {
            item.moveTo(AccessorialScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialScore("code-136", "label-146")
        item.status = AccessorialScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialScoreStatus.REQUESTED) }
    }
}
