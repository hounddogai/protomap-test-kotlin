package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialRequest("code-141", "name-538")
        assertEquals(AccessorialRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAccessorialRequest("code-141", "name-538")
        try {
            item.moveTo(AccessorialRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialRequest("code-141", "name-538")
        item.status = AccessorialRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialRequestStatus.REQUESTED) }
    }
}
