package com.acme.transport.freightpay.accessorial.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newAccessorialBaseline("code-3", "title-431")
        assertEquals(AccessorialBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAccessorialBaseline("code-3", "title-431")
        try {
            item.moveTo(AccessorialBaselineStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialBaselineStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAccessorialBaseline("code-3", "title-431")
        item.status = AccessorialBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(AccessorialBaselineStatus.PLANNED) }
    }
}
