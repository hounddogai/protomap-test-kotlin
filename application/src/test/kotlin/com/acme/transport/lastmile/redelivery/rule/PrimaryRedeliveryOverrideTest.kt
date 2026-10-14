package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRedeliveryOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryRedeliveryOverride("code-735", "label-905")
        assertEquals(PrimaryRedeliveryOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPrimaryRedeliveryOverride("code-735", "label-905")
        try {
            item.moveTo(PrimaryRedeliveryOverrideStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRedeliveryOverrideStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryRedeliveryOverride("code-735", "label-905")
        item.status = PrimaryRedeliveryOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryRedeliveryOverrideStatus.DRAFT) }
    }
}
