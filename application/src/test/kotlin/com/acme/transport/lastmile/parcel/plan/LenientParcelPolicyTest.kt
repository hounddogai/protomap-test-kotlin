package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientParcelPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientParcelPolicy("code-460", "name-462")
        assertEquals(LenientParcelPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientParcelPolicy("code-460", "name-462")
        try {
            item.moveTo(LenientParcelPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientParcelPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientParcelPolicy("code-460", "name-462")
        item.status = LenientParcelPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientParcelPolicyStatus.REQUESTED) }
    }
}
