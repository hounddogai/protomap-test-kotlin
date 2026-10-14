package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newServiceAreaRecord("code-82", "title-282")
        assertEquals(ServiceAreaRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newServiceAreaRecord("code-82", "title-282")
        try {
            item.moveTo(ServiceAreaRecordStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaRecordStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServiceAreaRecord("code-82", "title-282")
        item.status = ServiceAreaRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(ServiceAreaRecordStatus.REQUESTED) }
    }
}
