package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAccessorialRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveAccessorialRequest("code-559", "label-226")
        assertEquals(AdaptiveAccessorialRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveAccessorialRequest("code-559", "label-226")
        try {
            item.moveTo(AdaptiveAccessorialRequestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAccessorialRequestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveAccessorialRequest("code-559", "label-226")
        item.status = AdaptiveAccessorialRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveAccessorialRequestStatus.REQUESTED) }
    }
}
