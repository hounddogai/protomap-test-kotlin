package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServiceAreaRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientServiceAreaRequest("code-387", "label-744")
        assertEquals(LenientServiceAreaRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientServiceAreaRequest("code-387", "label-744")
        try {
            item.moveTo(LenientServiceAreaRequestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServiceAreaRequestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientServiceAreaRequest("code-387", "label-744")
        item.status = LenientServiceAreaRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientServiceAreaRequestStatus.PROPOSED) }
    }
}
