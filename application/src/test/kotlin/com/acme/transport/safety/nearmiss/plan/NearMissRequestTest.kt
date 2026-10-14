package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newNearMissRequest("code-373", "label-291")
        assertEquals(NearMissRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newNearMissRequest("code-373", "label-291")
        try {
            item.moveTo(NearMissRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newNearMissRequest("code-373", "label-291")
        item.status = NearMissRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(NearMissRequestStatus.REQUESTED) }
    }
}
