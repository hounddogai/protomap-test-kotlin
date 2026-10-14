package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newServiceAreaException("code-885", "title-49")
        assertEquals(ServiceAreaExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newServiceAreaException("code-885", "title-49")
        try {
            item.moveTo(ServiceAreaExceptionStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaExceptionStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServiceAreaException("code-885", "title-49")
        item.status = ServiceAreaExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServiceAreaExceptionStatus.OPEN) }
    }
}
