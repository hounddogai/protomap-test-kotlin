package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAccessorialPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedAccessorialPolicy("code-77", "name-421")
        assertEquals(CappedAccessorialPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedAccessorialPolicy("code-77", "name-421")
        try {
            item.moveTo(CappedAccessorialPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAccessorialPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedAccessorialPolicy("code-77", "name-421")
        item.status = CappedAccessorialPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedAccessorialPolicyStatus.REQUESTED) }
    }
}
