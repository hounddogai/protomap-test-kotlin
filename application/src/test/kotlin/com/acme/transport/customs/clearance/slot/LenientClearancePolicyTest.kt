package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientClearancePolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientClearancePolicy("code-601", "label-119")
        assertEquals(LenientClearancePolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientClearancePolicy("code-601", "label-119")
        try {
            item.moveTo(LenientClearancePolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientClearancePolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientClearancePolicy("code-601", "label-119")
        item.status = LenientClearancePolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientClearancePolicyStatus.REQUESTED) }
    }
}
