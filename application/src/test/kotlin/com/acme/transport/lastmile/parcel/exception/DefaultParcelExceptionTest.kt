package com.acme.transport.lastmile.parcel.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultParcelExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultParcelException("code-300", "label-641")
        assertEquals(DefaultParcelExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefaultParcelException("code-300", "label-641")
        try {
            item.moveTo(DefaultParcelExceptionStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultParcelExceptionStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultParcelException("code-300", "label-641")
        item.status = DefaultParcelExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultParcelExceptionStatus.DRAFT) }
    }
}
