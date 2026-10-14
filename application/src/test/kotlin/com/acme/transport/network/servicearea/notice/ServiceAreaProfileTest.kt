package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newServiceAreaProfile("code-86", "name-62")
        assertEquals(ServiceAreaProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newServiceAreaProfile("code-86", "name-62")
        try {
            item.moveTo(ServiceAreaProfileStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaProfileStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newServiceAreaProfile("code-86", "name-62")
        item.status = ServiceAreaProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ServiceAreaProfileStatus.PROPOSED) }
    }
}
