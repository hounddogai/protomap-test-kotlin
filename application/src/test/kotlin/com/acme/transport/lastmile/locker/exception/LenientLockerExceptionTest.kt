package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLockerExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newLenientLockerException("code-311", "label-461")
        assertEquals(LenientLockerExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientLockerException("code-311", "label-461")
        try {
            item.moveTo(LenientLockerExceptionStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLockerExceptionStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientLockerException("code-311", "label-461")
        item.status = LenientLockerExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(LenientLockerExceptionStatus.CREATED) }
    }
}
