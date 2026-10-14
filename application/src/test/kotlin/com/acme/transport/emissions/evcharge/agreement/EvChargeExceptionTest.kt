package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeExceptionTest {
    @Test
    fun `starts in proposed`() {
        val item = newEvChargeException("code-434", "label-688")
        assertEquals(EvChargeExceptionStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newEvChargeException("code-434", "label-688")
        try {
            item.moveTo(EvChargeExceptionStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeExceptionStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newEvChargeException("code-434", "label-688")
        item.status = EvChargeExceptionStatus.WITHDRAWN
        assertFails("move") { item.moveTo(EvChargeExceptionStatus.PROPOSED) }
    }
}
