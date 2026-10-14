package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialPolicy("code-563", "label-647")
        assertEquals(AccessorialPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newAccessorialPolicy("code-563", "label-647")
        try {
            item.moveTo(AccessorialPolicyStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialPolicyStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialPolicy("code-563", "label-647")
        item.status = AccessorialPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialPolicyStatus.REQUESTED) }
    }
}
