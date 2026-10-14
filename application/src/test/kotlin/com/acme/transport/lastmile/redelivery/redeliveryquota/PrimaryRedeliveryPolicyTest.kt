package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRedeliveryPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryRedeliveryPolicy("code-351", "name-263")
        assertEquals(PrimaryRedeliveryPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryRedeliveryPolicy("code-351", "name-263")
        try {
            item.moveTo(PrimaryRedeliveryPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRedeliveryPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryRedeliveryPolicy("code-351", "name-263")
        item.status = PrimaryRedeliveryPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryRedeliveryPolicyStatus.CREATED) }
    }
}
