package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyHoldExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyHoldException("code-429", "name-14")
        assertEquals(LegacyHoldExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyHoldException("code-429", "name-14")
        try {
            item.moveTo(LegacyHoldExceptionStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyHoldExceptionStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyHoldException("code-429", "name-14")
        item.status = LegacyHoldExceptionStatus.FAILED
        assertFails("move") { item.moveTo(LegacyHoldExceptionStatus.NEW) }
    }
}
